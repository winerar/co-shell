package app.coshell.core.domain.family

import app.coshell.core.model.Family
import app.coshell.core.model.Invite
import app.coshell.core.model.Member

/**
 * Family documents on the server (Firestore in `core:data`). Calls throw on network errors;
 * "no access" is not an error — it is `null` / `false`.
 */
interface FamilyRemote {
    /** The family if the signed-in user may read it (a member or invited), else `null`. */
    suspend fun family(familyId: String): Family?

    /** Invitation for the signed-in user's own email, if any. */
    suspend fun inviteFor(email: String): Invite?

    /** Creates the family with [me] as owner; `false` if it already exists. */
    suspend fun create(familyId: String, me: Member): Boolean

    /** Adds [me] to the family of [invite]. */
    suspend fun join(invite: Invite, me: Member)

    suspend fun invite(familyId: String, email: String, invitedBy: String)
    suspend fun invites(familyId: String): List<Invite>
    suspend fun cancelInvite(email: String)

    companion object {
        /**
         * The app serves one family per Firebase project (owner's requirement: only our family).
         * Security rules allow creating only this id, so nobody else can start a family here.
         */
        const val FAMILY_ID = "main"
    }
}

/** Normalized email for invitations: Google emails are case-insensitive. */
fun normalizeEmail(email: String): String = email.trim().lowercase()

/** Light check for the invite form; Google decides what really exists. */
fun isPlausibleEmail(email: String): Boolean =
    Regex("^[^@\\s/]+@[^@\\s/]+\\.[^@\\s/]+$").matches(normalizeEmail(email))
