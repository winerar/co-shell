package app.coshell.core.domain.greeting

/**
 * One-time personal greeting of release 1.0 (notification + pill over «Обзор»). Pure rule, no Android.
 *
 * Shown only to a signed-in family member who is **not** the family owner (the owner asked for it), only on
 * version [FIRST_MAJOR] and newer, and until it is shown once. Names and emails are not in the code on purpose.
 */
object ReleaseGreeting {
    const val FIRST_MAJOR = 1

    /** `1.4` → 1, `1.4-debug` → 1, `0.0-debug` → 0; `null` for a name without a number. */
    fun majorOf(versionName: String): Int? = versionName.substringBefore('.').toIntOrNull()

    fun isDue(versionName: String, isMember: Boolean, isOwner: Boolean): Boolean =
        isMember && !isOwner && (majorOf(versionName) ?: 0) >= FIRST_MAJOR
}
