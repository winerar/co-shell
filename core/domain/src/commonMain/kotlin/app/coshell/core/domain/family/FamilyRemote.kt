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

    /** Id of the family [uid] is in, by the person's pointer (`users/{uid}`); `null` — none (or a member of the old `main` family). */
    suspend fun pointer(uid: String): String?

    /** Invitation for the signed-in user's own email, if any. */
    suspend fun inviteFor(email: String): Invite?

    /** Creates the family with [me] as owner and points [me] at it; `false` if the id is taken. */
    suspend fun create(familyId: String, me: Member): Boolean

    /** Adds [me] to the family of [invite] and points [me] at it, in one write. */
    suspend fun join(invite: Invite, me: Member)

    /** Remembers that [uid] is in [familyId] (a member of the old `main` family had no pointer). Best effort. */
    suspend fun setPointer(uid: String, familyId: String)

    /**
     * Takes [uid] out of the family: the person themselves ([me] == [uid], the pointer goes too) or the owner
     * removing them (the pointer stays: that is how the removed phone learns it).
     */
    suspend fun remove(familyId: String, uid: String, me: String)

    /** Deletes the family with its records and invitations (owner only) and [me]'s pointer. */
    suspend fun deleteFamily(familyId: String, me: String)

    /** Drops [uid]'s own pointer (after being removed, or before deleting the account). */
    suspend fun clearPointer(uid: String)

    suspend fun invite(familyId: String, email: String, invitedBy: String)
    suspend fun invites(familyId: String): List<Invite>
    suspend fun cancelInvite(email: String)

    companion object {
        /**
         * The first family of the project (the owner's own) keeps this id; every new family gets a random one
         * ([newFamilyId]). Old app versions only know this id.
         */
        const val LEGACY_FAMILY_ID = "main"

        /** Random, unguessable, at least 20 characters (rules refuse shorter ids for new families). */
        fun newFamilyId(random: kotlin.random.Random = kotlin.random.Random): String {
            val alphabet = "abcdefghijklmnopqrstuvwxyz0123456789"
            return "f" + CharArray(27) { alphabet[random.nextInt(alphabet.length)] }.concatToString()
        }
    }
}

/** Normalized email for invitations: Google emails are case-insensitive. */
fun normalizeEmail(email: String): String = email.trim().lowercase()

/** Light check for the invite form; Google decides what really exists. */
fun isPlausibleEmail(email: String): Boolean =
    Regex("^[^@\\s/]+@[^@\\s/]+\\.[^@\\s/]+$").matches(normalizeEmail(email))

/**
 * Names of the *other* family members by uid — for «who added it» in lists. Empty when not in a
 * family or alone in it, so single-user screens stay as they were.
 */
fun interface OtherMembers {
    fun names(): kotlinx.coroutines.flow.Flow<Map<String, String>>
}
