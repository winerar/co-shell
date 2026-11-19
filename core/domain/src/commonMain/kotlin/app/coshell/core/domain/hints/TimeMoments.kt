package app.coshell.core.domain.hints

/** Dates and counts the time-based warm moments need. Days are epoch days; [entryCount] is capped by the caller. */
data class MomentFacts(
    val today: Long,
    val firstSeenDay: Long,
    val firstEntryDay: Long?,
    val lastEntryDay: Long?,
    val entryCount: Int,
)

/** Which time-based moments are due (`hints.md` §«Тёплые моменты»); the planner still shows each only once. */
object TimeMoments {
    const val THEME_AFTER_DAYS = 3
    const val BACKUP_ENTRIES = 30
    const val BACKUP_AFTER_DAYS = 14
    const val FIRST_WEEK_DAYS = 7
    const val AWAY_DAYS = 3

    fun due(f: MomentFacts): Set<HintId> = buildSet {
        val hasEntries = f.entryCount > 0
        if (hasEntries && f.firstEntryDay != null && f.today - f.firstEntryDay >= FIRST_WEEK_DAYS) add(HintId.FIRST_WEEK)
        if (hasEntries && f.lastEntryDay != null && f.today - f.lastEntryDay >= AWAY_DAYS) add(HintId.WELCOME_BACK)
        if (f.entryCount >= BACKUP_ENTRIES || (hasEntries && f.today - f.firstSeenDay >= BACKUP_AFTER_DAYS)) add(HintId.BACKUP)
        if (f.today - f.firstSeenDay >= THEME_AFTER_DAYS) add(HintId.THEME)
    }
}
