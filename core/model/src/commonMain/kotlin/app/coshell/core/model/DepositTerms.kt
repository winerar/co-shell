package app.coshell.core.model

import kotlinx.datetime.LocalDate

/**
 * Conditions of a deposit (#634, step 3): the yearly [rateBp] in basis points (800 = 8 %), the [endDate] the deposit closes and how
 * the interest is [payout]; the optional [startDate] (opened on) lets «в конце срока» count the whole term, not only what is left. Only the expected income is counted from them; the app writes no operation by itself and takes no tax.
 */
data class DepositTerms(val rateBp: Int, val endDate: LocalDate, val payout: DepositPayout, val startDate: LocalDate? = null) {
    init {
        require(rateBp in 1..10_000) { "Rate must be between 0.01 and 100 percent" }
        require(startDate == null || startDate <= endDate) { "A deposit opens before it closes" }
    }
}

/** How the interest of a deposit is paid: all at the end, every month to the owner, or every month added to the deposit. */
enum class DepositPayout { AT_END, MONTHLY, CAPITALIZED }
