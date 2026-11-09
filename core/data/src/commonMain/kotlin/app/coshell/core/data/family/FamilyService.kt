package app.coshell.core.data.family

import app.coshell.core.data.auth.AuthGateway
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.db.SyncStateEntity
import app.coshell.core.data.remote.ApiException
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.data.sync.SyncResult
import app.coshell.core.domain.BackupRepository
import app.coshell.core.domain.family.FamilyRemote
import app.coshell.core.domain.family.FamilyRemote.Companion.FAMILY_ID
import app.coshell.core.model.Family
import app.coshell.core.model.Invite
import app.coshell.core.model.Member
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Sign-in, family membership and sync in one place (docs/ARCHITECTURE.md §10).
 * Membership is cached in `sync_state`, so background sync works without asking the server
 * every time and the app knows member names offline.
 *
 * [safetyBackup] saves a JSON copy of this phone's data before it is replaced by the family's.
 */
class FamilyService(
    private val configured: Boolean,
    private val auth: AuthGateway,
    private val remote: FamilyRemote,
    private val dao: SyncDao,
    private val engine: SyncEngine,
    private val backup: BackupRepository,
    private val safetyBackup: suspend (json: String) -> Unit,
) {
    private val _state = MutableStateFlow<FamilyState>(if (configured) FamilyState.SignedOut else FamilyState.NotConfigured)
    val state: StateFlow<FamilyState> = _state.asStateFlow()

    /** Cached family for the list screens (author names); `null` — not in a family. */
    val joined: Family? get() = (_state.value as? FamilyState.Joined)?.family

    private var loaded = false

    /** Other members' names by uid (see [app.coshell.core.domain.family.OtherMembers]). */
    fun otherMemberNames(): Flow<Map<String, String>> = _state.map { s ->
        (s as? FamilyState.Joined)?.let { j -> j.family.members.filter { it.uid != j.me.uid }.associate { it.uid to it.name.ifBlank { it.email } } }
            .orEmpty()
    }.distinctUntilChanged()

    /** Restores the state from the cache without network. Only the first call does anything. */
    suspend fun load() {
        if (!configured || loaded) return
        loaded = true
        val session = auth.session.value ?: return run { _state.value = FamilyState.SignedOut }
        val cached = cachedFamily()
        val me = cached?.member(session.uid)
        _state.value = if (cached != null && me != null) FamilyState.Joined(cached, me) else FamilyState.Checking(session.email)
    }

    /** Asks the server who we are in the family. Network errors keep a cached membership. */
    suspend fun refresh() {
        if (!configured) return
        val session = auth.session.value ?: return run { _state.value = FamilyState.SignedOut }
        try {
            val family = remote.family(FAMILY_ID)
            val me = family?.member(session.uid)
            _state.value = when {
                family != null && me != null -> { cache(family); FamilyState.Joined(family, me) }
                else -> {
                    cache(null)
                    remote.inviteFor(session.email)?.let { FamilyState.Invited(session.email, it) }
                        ?: FamilyState.NoAccess(session.email)
                }
            }
        } catch (e: ApiException) {
            if (e.isUnauthenticated) signOut() else offline(session.email)
        } catch (e: Exception) {
            offline(session.email)
        }
    }

    suspend fun signIn(googleIdToken: String) {
        auth.signInWithGoogle(googleIdToken)
        refresh()
    }

    suspend fun signOut() {
        auth.signOut()
        cache(null)
        _state.value = if (configured) FamilyState.SignedOut else FamilyState.NotConfigured
    }

    /** Creates the family with this phone's data in it. `false` — a family already exists (need an invite). */
    suspend fun createFamily(): Boolean {
        val me = currentMember() ?: return false
        if (!remote.create(FAMILY_ID, me)) return false
        dao.markAllPending()
        refresh()
        return true
    }

    /** Does this phone hold its own operations? Then joining asks whether to keep them. */
    suspend fun hasLocalData(): Boolean = dao.liveTransactionCount() > 0

    /**
     * Joins the family of the pending invitation. [keepLocalData] = false replaces this phone's
     * data with the family's (a safety copy is saved first); true sends it to the family too.
     */
    suspend fun join(keepLocalData: Boolean) {
        val invited = _state.value as? FamilyState.Invited ?: return
        val me = currentMember() ?: return
        remote.join(invited.invite, me)
        if (keepLocalData) {
            dao.markAllPending()
        } else {
            safetyBackup(backup.exportJson())
            dao.wipeSyncedData()
        }
        refresh()
    }

    suspend fun invite(email: String) {
        val joined = _state.value as? FamilyState.Joined ?: return
        remote.invite(joined.family.id, email, joined.me.uid)
    }

    suspend fun invites(): List<Invite> {
        val joined = _state.value as? FamilyState.Joined ?: return emptyList()
        return remote.invites(joined.family.id)
    }

    suspend fun cancelInvite(email: String) = remote.cancelInvite(email)

    /**
     * One sync run if this phone is in the family, else `null`. While the family has a single
     * member, the member list is re-read too, so the owner sees the second person appear.
     * A permission error means we were removed — the state is re-checked.
     */
    suspend fun sync(): SyncResult? {
        val joined = _state.value as? FamilyState.Joined ?: return null
        return try {
            engine.sync().also { if (joined.family.members.size < 2) refresh() }
        } catch (e: ApiException) {
            if (e.isPermissionDenied || e.isUnauthenticated) refresh()
            throw e
        }
    }

    private fun offline(email: String) {
        if (_state.value !is FamilyState.Joined) _state.value = FamilyState.Offline(email)
    }

    private fun currentMember(): Member? =
        auth.session.value?.let { Member(it.uid, it.name, it.email) }

    private suspend fun cachedFamily(): Family? =
        dao.state(FAMILY_KEY)?.let { runCatching { json.decodeFromString<CachedFamily>(it).toModel() }.getOrNull() }

    private suspend fun cache(family: Family?) =
        dao.setState(SyncStateEntity(FAMILY_KEY, family?.let { json.encodeToString(CachedFamily.of(it)) } ?: ""))

    @Serializable
    private data class CachedFamily(val id: String, val ownerUid: String, val members: List<CachedMember>) {
        fun toModel() = Family(id, ownerUid, members.map { Member(it.uid, it.name, it.email) })
        companion object {
            fun of(f: Family) = CachedFamily(f.id, f.ownerUid, f.members.map { CachedMember(it.uid, it.name, it.email) })
        }
    }

    @Serializable
    private data class CachedMember(val uid: String, val name: String, val email: String)

    private companion object {
        const val FAMILY_KEY = "family"
        val json = Json { ignoreUnknownKeys = true }
    }
}
