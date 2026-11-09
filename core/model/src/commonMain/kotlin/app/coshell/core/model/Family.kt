package app.coshell.core.model

/** A family member: Firebase user id, name and Google email. */
data class Member(val uid: String, val name: String, val email: String)

/** The family that shares one budget. The first member who created it is the [ownerUid]. */
data class Family(val id: String, val ownerUid: String, val members: List<Member>) {
    fun member(uid: String?): Member? = members.firstOrNull { it.uid == uid }
    fun isMember(uid: String) = member(uid) != null
}

/** Invitation for a Google account (lower-case [email]) to join [familyId]. */
data class Invite(val email: String, val familyId: String, val invitedBy: String)
