package app.coshell.core.domain.settings

import app.coshell.core.domain.budgets.DEFAULT_MONTH_START_DAY
import kotlinx.coroutines.flow.Flow

/**
 * Day the family's budget month starts on (#139: 1st by default, e.g. payday otherwise). A family
 * setting: shared by both phones through sync (#122), works without a family too.
 */
interface MonthStartSettings {
    /** Always in [DAYS]. */
    val monthStartDay: Flow<Int>

    /** [day] outside [DAYS] is refused with [IllegalArgumentException]. */
    suspend fun setMonthStartDay(day: Int)

    companion object {
        const val DEFAULT: Int = DEFAULT_MONTH_START_DAY
        val DAYS: IntRange = 1..31

        /** A stored value read back: missing, broken or out of range → [DEFAULT]. */
        fun parse(stored: String?): Int = stored?.trim()?.toIntOrNull()?.takeIf { it in DAYS } ?: DEFAULT
    }
}
