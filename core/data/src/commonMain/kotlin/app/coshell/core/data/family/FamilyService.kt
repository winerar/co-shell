package app.coshell.core.data.family

import app.coshell.core.data.auth.AuthGateway
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.db.SyncStateEntity
import app.coshell.core.data.remote.ApiException
import app.coshell.core.data.sync.ChangeBell
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.data.sync.SyncIssue
import app.coshell.core.data.sync.SyncResult
import app.coshell.core.domain.BackupRepository
import app.coshell.core.domain.family.FamilyRemote
import app.coshell.core.domain.family.FamilyRemote.Companion.LEGACY_FAMILY_ID
import app.coshell.core.data.sync.VersionGate
import app.coshell.core.model.Family
import app.coshell.core.model.Invite
import app.coshell.core.model.Member
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

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
    private val clock: Clock = Clock.System,
    private val bell: ChangeBell = ChangeBell.None,
    private val currentFamily: CurrentFamily = CurrentFamily(),
    private val newFamilyId: () -> String = { FamilyRemote.newFamilyId() },
    private val safetyBackup: suspend (json: String) -> Unit,
) {
    private val _state = MutableStateFlow<FamilyState>(if (configured) FamilyState.SignedOut else FamilyState.NotConfigured)
    val state: StateFlow<FamilyState> = _state.asStateFlow()

    /** Cached family for the list screens (author names); `null` — not in a family. */
    val joined: Family? get() = (_state.value as? FamilyState.Joined)?.family

    private val familyCache = FamilyCache(dao)
    private var loaded = false
    private var membersCheckedAt: Instant? = null

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
        val cached = familyCache.load()
        val me = cached?.member(session.uid)
        currentFamily.id = if (cached != null && me != null) cached.id else null
        _state.value = if (cached != null && me != null) FamilyState.Joined(cached, me) else FamilyState.Checking(session.email)
    }

    /** Asks the server who we are in the family. Network errors keep a cached membership. */
    suspend fun refresh() {
        if (!configured) return
        val session = auth.session.value ?: return run { _state.value = FamilyState.SignedOut }
        try {
            _state.value = resolve(session.uid, session.email)
        } catch (e: ApiException) {
            if (e.isUnauthenticated) {
                signOut()
                recordIssue(e) // after signOut, which clears it: the banner offers «Войти»
            } else {
                offline(session.email)
            }
        } catch (e: Exception) {
            offline(session.email)
        }
    }

    /**
     * Which family this person is in. While a membership is known (cache) only that family is read — the same one
     * read per check as before. Otherwise the pointer `users/{uid}` names the family; a member of the old `main`
     * family has no pointer yet (it is written now). Not a member any more after being one → [FamilyState.Removed].
     */
    private suspend fun resolve(uid: String, email: String): FamilyState {
        val known = (_state.value as? FamilyState.Joined)?.family?.id ?: familyCache.load()?.id
        val pointer = if (known == null) remote.pointer(uid) else null
        val id = known ?: pointer ?: LEGACY_FAMILY_ID
        val family = remote.family(id)
        val me = family?.member(uid)
        if (family != null && me != null) {
            if (known == null && pointer == null) runCatching { remote.setPointer(uid, id) } // old `main` members
            familyCache.save(family)
            currentFamily.id = family.id
            return FamilyState.Joined(family, me)
        }
        val wasIn = known != null || pointer != null
        familyCache.save(null)
        currentFamily.id = null
        if (known != null) resetSyncPosition()
        return remote.inviteFor(email)?.let { FamilyState.Invited(email, it) }
            ?: if (wasIn) FamilyState.Removed(email) else FamilyState.NoAccess(email)
    }

    /** The pull cursor and peer schema belong to one family: forget them when the family changes. */
    private suspend fun resetSyncPosition() {
        dao.setState(SyncStateEntity(SyncEngine.CURSOR, ""))
        dao.setState(SyncStateEntity(VersionGate.KEY, ""))
    }

    suspend fun signIn(googleIdToken: String) {
        auth.signInWithGoogle(googleIdToken)
        clearIssue()
        refresh()
    }

    suspend fun signOut() {
        // Removed from the family: its trace on the server goes too, or the next sign-in would show it again.
        if (_state.value is FamilyState.Removed) auth.uid?.let { runCatching { remote.clearPointer(it) } }
        auth.signOut()
        familyCache.save(null)
        currentFamily.id = null
        clearIssue()
        _state.value = if (configured) FamilyState.SignedOut else FamilyState.NotConfigured
    }

    /** Creates the family with this phone's data in it. `false` — a family already exists (need an invite). */
    suspend fun createFamily(): Boolean {
        val me = currentMember() ?: return false
        val id = newFamilyId()
        if (!remote.create(id, me)) return false
        familyCache.save(null)
        resetSyncPosition()
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
        resetSyncPosition()
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

    /** A member leaves (the owner cannot: they delete the family). The data on the phone stays; sync stops. */
    suspend fun leave() {
        val joined = _state.value as? FamilyState.Joined ?: return
        check(!joined.family.isOwner(joined.me.uid)) { "The owner deletes the family instead of leaving" }
        remote.remove(joined.family.id, joined.me.uid, joined.me.uid)
        forgetFamily()
    }

    /** The owner removes [uid] from the family; their phone learns it on its next check. */
    suspend fun removeMember(uid: String) {
        val joined = _state.value as? FamilyState.Joined ?: return
        check(joined.family.isOwner(joined.me.uid) && uid != joined.me.uid) { "Only the owner removes others" }
        remote.remove(joined.family.id, uid, joined.me.uid)
        refresh()
    }

    /** The owner deletes the family and its cloud data for everyone; data on the phones stays. */
    suspend fun deleteFamily() {
        val joined = _state.value as? FamilyState.Joined ?: return
        check(joined.family.isOwner(joined.me.uid)) { "Only the owner deletes the family" }
        remote.deleteFamily(joined.family.id, joined.me.uid)
        forgetFamily()
    }

    /**
     * Deletes the cloud account: the family (owner) or the membership (member), the pointer, the Firebase user.
     * Data on the phone stays. [ApiException.isLoginTooOld] — sign in again and repeat; nothing is lost by repeating.
     */
    suspend fun deleteAccount() {
        val uid = auth.uid ?: return
        when (val s = _state.value) {
            is FamilyState.Joined ->
                if (s.family.isOwner(uid)) remote.deleteFamily(s.family.id, uid) else remote.remove(s.family.id, uid, uid)
            else -> runCatching { remote.clearPointer(uid) }
        }
        auth.deleteAccount()
        familyCache.save(null)
        currentFamily.id = null
        resetSyncPosition()
        clearIssue()
        _state.value = if (configured) FamilyState.SignedOut else FamilyState.NotConfigured
    }

    /** Out of the family on the server: forget it here and ask again where the person stands (no access, or invited). */
    private suspend fun forgetFamily() {
        familyCache.save(null)
        currentFamily.id = null
        resetSyncPosition()
        _state.value = FamilyState.Checking(auth.session.value?.email.orEmpty())
        refresh()
    }

    /**
     * One sync run if this phone is in the family, else `null`. While the family has a single
     * member, the member list is re-read too (at most every [MEMBERS_CHECK], it costs a Firestore
     * read — on every 10-second poll it doubled the reads), so the owner sees the second person appear.
     * A permission error means we were removed — the state is re-checked. After sending edits it
     * rings the [bell], so the other phone pulls at once instead of on its next poll.
     */
    suspend fun sync(): SyncResult? {
        val joined = _state.value as? FamilyState.Joined ?: return null
        return try {
            engine.sync().also {
                if (!it.pushPaused) clearIssue() // paused: the quota banner stays until edits are sent
                if (it.pushed > 0) ringQuietly()
                if (joined.family.members.size < 2) refreshMembersThrottled()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            if (e.isPermissionDenied || e.isUnauthenticated) refresh()
            recordIssue(e)
            throw e
        } catch (e: Exception) {
            recordIssue(e)
            throw e
        }
    }

    /** Remembers why sync failed ([SyncIssue]) for the banner on «Обзор». */
    private suspend fun recordIssue(error: Throwable) {
        val issue = SyncIssue.of(error, clock.now(), SyncIssue.decode(dao.state(SyncIssue.KEY)))
        dao.setState(SyncStateEntity(SyncIssue.KEY, issue.encode()))
    }

    private suspend fun clearIssue() {
        if (!dao.state(SyncIssue.KEY).isNullOrEmpty()) dao.setState(SyncStateEntity(SyncIssue.KEY, ""))
    }

    /** The ring is a speed-up only: if it fails, the other phone still gets the edits by polling. */
    private suspend fun ringQuietly() {
        try {
            bell.ring()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // ignored on purpose, see above
        }
    }

    private suspend fun refreshMembersThrottled() {
        val now = clock.now()
        val last = membersCheckedAt
        if (last != null && now - last < MEMBERS_CHECK) return
        membersCheckedAt = now
        refresh()
    }

    private fun offline(email: String) {
        if (_state.value !is FamilyState.Joined) _state.value = FamilyState.Offline(email)
    }

    /** The signed-in member's uid while in a family (for «Трата партнёра»: whose changes are not mine). */
    fun myUid(): String? = (_state.value as? FamilyState.Joined)?.me?.uid

    private fun currentMember(): Member? =
        auth.session.value?.let { Member(it.uid, it.name, it.email) }

    private companion object {
        val MEMBERS_CHECK = 1.minutes
    }
}
