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

    /** Epoch day of the first launch with hints; remembered by the first call, [today] is stored then. */
    suspend fun firstSeenDay(today: Long): Long

    /** Epoch day the first entry was noticed; stores [candidate] only if nothing is stored yet and it is not null. */
    suspend fun firstEntryDay(candidate: Long?): Long?

    /** «Показать подсказки заново»: forgets «shown», unhides the steps, turns hints on. */
    suspend fun reset()

    /**
     * «Знакомство» (#598): true once this phone was sorted into «new install» or «already in use». Survives [reset], so the
     * card comes back after «Показать подсказки заново» but never pops up on a phone that had data when the tour appeared.
     */
    suspend fun tourChecked(): Boolean = true
    suspend fun setTourChecked() = Unit
}

/**
 * Picks the one hint to show now (`hints.md` §«Когда показывать»): hints on, at most one word or gesture per visit and
 * one per day, the first by priority among those whose condition holds, each only once. Warm moments ([HintKind.MOMENT])
 * are shown by their event and sit outside the visit/day limit (owner poll 2026-11-19).
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
        if (!enabled) return null
        val limited = shownThisVisit || lastShownDay == today
        return HintId.entries.firstOrNull { it in candidates && it.key !in shown && (!limited || it.kind == HintKind.MOMENT) }
    }
}
