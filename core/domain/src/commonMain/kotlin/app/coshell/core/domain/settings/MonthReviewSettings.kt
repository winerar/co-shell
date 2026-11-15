package app.coshell.core.domain.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

/** «Итог месяца» on «Обзор» (#236): the month whose card the user hid with ✕. Kept on the phone only. */
interface MonthReviewSettings {
    /** `null` — never hidden. */
    val dismissed: Flow<YearMonth?>

    suspend fun dismiss(month: YearMonth)

    companion object {
        /** A stored value read back («2026-10»): missing or broken → `null`. */
        fun parse(stored: String?): YearMonth? = stored?.trim()?.let { runCatching { YearMonth.parse(it) }.getOrNull() }
    }
}
