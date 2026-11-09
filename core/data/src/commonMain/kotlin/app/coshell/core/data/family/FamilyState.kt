package app.coshell.core.data.family

import app.coshell.core.model.Family
import app.coshell.core.model.Invite
import app.coshell.core.model.Member

/** Where this phone is on the way to a shared budget; shown by the «Семья» screen. */
sealed interface FamilyState {
    /** The build has no Firebase keys (local builds): the app works offline only. */
    data object NotConfigured : FamilyState
    data object SignedOut : FamilyState
    data class Checking(val email: String) : FamilyState
    /** Signed in, but neither a member nor invited (issue #34: «нет доступа»). */
    data class NoAccess(val email: String) : FamilyState
    data class Invited(val email: String, val invite: Invite) : FamilyState
    data class Joined(val family: Family, val me: Member) : FamilyState
    /** Could not reach the server while checking; [email] is the signed-in account. */
    data class Offline(val email: String) : FamilyState
}
