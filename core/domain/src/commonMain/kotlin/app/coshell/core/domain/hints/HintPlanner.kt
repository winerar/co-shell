package app.coshell.core.domain.hints

/** What this phone remembers about hints (device settings, not synced, not in backups). Android: SharedPreferences. */
interface HintStore {
    suspend fun enabled(): Boolean
    suspend fun setEnabled(value: Boolean)
    suspend fun shown(): Set<String>
    suspend fun markShown(key: String)

    /** Epoch day of the last hint shown, or null. */
    suspend fun lastShownDay(): Long?
    suspend fun setLastShownDay(day: Long)

    /** «Скрыть шаги» on the first-steps sheet. */
    suspend fun stepsHidden(): Boolean
    suspend fun setStepsHidden(value: Boolean)

    /** «Показать подсказки заново»: forgets «shown», unhides the steps, turns hints on. */
    suspend fun reset()
}

/**
 * Picks the one hint to show now (`hints.md` §«Когда показывать»): hints on, at most one per visit and one per day,
 * the first by priority among those whose condition holds, each only once.
 */
object HintPlanner {
    fun next(
        candidates: Set<HintId>,
        enabled: Boolean,
        shown: Set<String>,
        lastShownDay: Long?,
        today: Long,
        shownThisVisit: Boolean,
    ): HintId? {
        if (!enabled || shownThisVisit || lastShownDay == today) return null
        return HintId.entries.firstOrNull { it in candidates && it.key !in shown }
    }
}
