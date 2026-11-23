package app.coshell.core.data.family

import app.coshell.core.data.auth.AuthGateway
import app.coshell.core.data.auth.AuthSession
import app.coshell.core.domain.family.FamilyRemote
import app.coshell.core.domain.family.normalizeEmail
import app.coshell.core.model.Family
import app.coshell.core.model.Invite
import app.coshell.core.model.Member
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuth(initial: AuthSession? = null) : AuthGateway {
    override val session = MutableStateFlow(initial)
    override suspend fun signInWithGoogle(googleIdToken: String): AuthSession =
        session(googleIdToken).also { session.value = it }
    override fun signOut() { session.value = null }
    var accountDeleted = false
    override suspend fun deleteAccount() { accountDeleted = true; session.value = null }
    override suspend fun idToken() = session.value!!.idToken

    companion object {
        fun session(user: String) = AuthSession(user, "$user@gmail.com", user, "token-$user", "refresh", Long.MAX_VALUE)
    }
}

/** Server-side family documents shared by several phones, enforcing the same rules in plain code. */
class FakeFamilyServer {
    val families = mutableMapOf<String, Family>()
    /** The first (usually only) family. */
    var family: Family?
        get() = families.values.firstOrNull()
        set(value) { families.clear(); value?.let { families[it.id] = it } }
    val invites = mutableMapOf<String, Invite>()
    /** `users/{uid}`: which family a person is in. */
    val pointers = mutableMapOf<String, String>()
    var offline = false
    /** How many times the family document was read (each is a billed Firestore read). */
    var familyReads = 0
}

class FakeFamilyRemote(private val server: FakeFamilyServer, private val auth: FakeAuth) : FamilyRemote {
    private fun uid() = auth.session.value!!.uid
    private fun check() { if (server.offline) error("offline") }

    override suspend fun family(familyId: String): Family? {
        check(); server.familyReads++
        return server.families[familyId]?.takeIf { it.isMember(uid()) }
    }
    override suspend fun pointer(uid: String): String? { check(); return server.pointers[uid] }
    override suspend fun inviteFor(email: String): Invite? { check(); return server.invites[normalizeEmail(email)] }
    override suspend fun create(familyId: String, me: Member): Boolean {
        check(); if (familyId in server.families) return false
        server.families[familyId] = Family(familyId, me.uid, listOf(me))
        server.pointers[me.uid] = familyId
        return true
    }
    override suspend fun join(invite: Invite, me: Member) {
        check()
        server.families[invite.familyId] = server.families.getValue(invite.familyId).let { it.copy(members = it.members + me) }
        server.pointers[me.uid] = invite.familyId
        server.invites.remove(normalizeEmail(invite.email)) // used up
    }
    override suspend fun setPointer(uid: String, familyId: String) { check(); server.pointers[uid] = familyId }
    override suspend fun remove(familyId: String, uid: String, me: String) {
        check()
        val family = server.families.getValue(familyId)
        server.families[familyId] = family.copy(members = family.members.filterNot { it.uid == uid })
        if (uid == me) server.pointers.remove(uid)
    }
    override suspend fun deleteFamily(familyId: String, me: String) {
        check(); server.families.remove(familyId); server.invites.values.removeAll { it.familyId == familyId }; server.pointers.remove(me)
    }
    override suspend fun clearPointer(uid: String) { check(); server.pointers.remove(uid) }
    override suspend fun invite(familyId: String, email: String, invitedBy: String) {
        server.invites[normalizeEmail(email)] = Invite(normalizeEmail(email), familyId, invitedBy)
    }
    override suspend fun invites(familyId: String) = server.invites.values.filter { it.familyId == familyId }
    override suspend fun cancelInvite(email: String) { server.invites.remove(normalizeEmail(email)) }
}
