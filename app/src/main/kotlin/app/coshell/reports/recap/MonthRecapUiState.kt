package app.coshell.reports.recap

import app.coshell.core.domain.reports.RecapCard
import app.coshell.core.domain.reports.RecapPhrase
import app.coshell.reports.UnusualLine
import kotlinx.datetime.YearMonth

/** «Итоги октября» (#554): the stories of a finished month, numbers already formatted. */
data class MonthRecapUiState(
    val isLoading: Boolean = false,
    val month: YearMonth? = null,
    val currency: String = "",
    val spent: String = "",
    /** Stories in order, the final one always last; empty — nothing in the month, no stories. */
    val cards: List<RecapCard> = emptyList(),
    val top: List<Share> = emptyList(),
    /** `null` — no month before to compare with. */
    val previousSpent: String? = null,
    /** Signed percent against the previous month: negative — lighter. */
    val changePercent: Int? = null,
    val weeks: List<Week> = emptyList(),
    val busiestWeek: Int? = null,
    val newCategory: NamedCategory? = null,
    val weekdayAverage: String? = null,
    val weekendAverage: String? = null,
    /** Weekend days cost more per day than weekdays; [weekendRatio] — the smaller average in shares of the bigger. */
    val weekendMore: Boolean = false,
    val weekendRatio: Float = 1f,
    val phrase: RecapPhrase = RecapPhrase.GOOD_START,
    /** The number inside the phrase: percent lighter, percent left, days in a row. */
    val phraseNumber: Int = 0,
    val recordedDays: Int = 0,
    /** The longest run of days with records; [streakFrom]–[streakTo] are its day numbers, [streakInOneMonth] — they can be told with one month name. */
    val streak: Int = 0,
    val streakFrom: Int = 0,
    val streakTo: Int = 0,
    val streakInOneMonth: Boolean = true,
    /** «Не как обычно» (#787): the rows of «Необычное» for the finished month. */
    val unusual: List<UnusualLine> = emptyList(),
) {
    data class NamedCategory(val id: String, /** `null` — deleted on another phone. */ val name: String?, val color: Long)

    data class Share(val category: NamedCategory, val amount: String, val percent: Int, val value: Long)

    /** «1–7»: the days of the week inside the period, [fraction] of the busiest week. */
    data class Week(val label: String, val amount: String, val fraction: Float)
}
