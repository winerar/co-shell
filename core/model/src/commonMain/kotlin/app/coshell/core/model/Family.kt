package app.coshell.core.model

/** A family member: Firebase user id, name and Google email. */
data class Member(val uid: String, val name: String, val email: String)

/** The family that shares one budget. The first member who created it is the [ownerUid]. */
data class Family(val id: String, val ownerUid: String, val members: List<Member>) {
    fun member(uid: String?): Member? = members.firstOrNull { it.uid == uid }
    fun isMember(uid: String) = member(uid) != null
    fun isOwner(uid: String?) = uid != null && uid == ownerUid
    /** No room for one more person ([MAX_MEMBERS], #661). */
    val isFull: Boolean get() = members.size >= MAX_MEMBERS

    companion object {
        /** People in one family; the same number is in firebase/firestore.rules (joining). */
        const val MAX_MEMBERS = 10
    }
}

/** Invitation for a Google account (lower-case [email]) to join [familyId]. */
data class Invite(val email: String, val familyId: String, val invitedBy: String)
