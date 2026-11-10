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
    override suspend fun idToken() = session.value!!.idToken

    companion object {
        fun session(user: String) = AuthSession(user, "$user@gmail.com", user, "token-$user", "refresh", Long.MAX_VALUE)
    }
}

/** Server-side family documents shared by several phones, enforcing the same rules in plain code. */
class FakeFamilyServer {
    var family: Family? = null
    val invites = mutableMapOf<String, Invite>()
    var offline = false
    /** How many times the family document was read (each is a billed Firestore read). */
    var familyReads = 0
}

class FakeFamilyRemote(private val server: FakeFamilyServer, private val auth: FakeAuth) : FamilyRemote {
    private fun uid() = auth.session.value!!.uid
    private fun check() { if (server.offline) error("offline") }

    override suspend fun family(familyId: String): Family? { check(); server.familyReads++; return server.family?.takeIf { it.isMember(uid()) } }
    override suspend fun inviteFor(email: String): Invite? { check(); return server.invites[normalizeEmail(email)] }
    override suspend fun create(familyId: String, me: Member): Boolean {
        check(); if (server.family != null) return false
        server.family = Family(familyId, me.uid, listOf(me)); return true
    }
    override suspend fun join(invite: Invite, me: Member) {
        check(); server.family = server.family!!.let { it.copy(members = it.members + me) }
    }
    override suspend fun invite(familyId: String, email: String, invitedBy: String) {
        server.invites[normalizeEmail(email)] = Invite(normalizeEmail(email), familyId, invitedBy)
    }
    override suspend fun invites(familyId: String) = server.invites.values.toList()
    override suspend fun cancelInvite(email: String) { server.invites.remove(normalizeEmail(email)) }
}
