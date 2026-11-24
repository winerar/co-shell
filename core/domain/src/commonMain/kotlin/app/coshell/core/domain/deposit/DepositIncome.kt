package app.coshell.core.domain.deposit

import app.coshell.core.model.DepositPayout
import app.coshell.core.model.DepositTerms
import app.coshell.core.model.Money
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus

/**
 * The expected income of a deposit (#634, step 3): the interest on [balance] from [today] to the closing day, before any tax.
 * Simple interest (balance × rate × days / 365) for «в конце срока» and «каждый месяц»; for «к вкладу» every whole month adds
 * a twelfth of the rate to the deposit, the rest of the days count simply. No balance, or a day past the end, means no income.
 */
object DepositIncome {
    fun expected(terms: DepositTerms, balance: Money, today: LocalDate): Money? {
        if (balance.amountMinor <= 0 || terms.endDate < today) return null
        val income = when (terms.payout) {
            DepositPayout.AT_END, DepositPayout.MONTHLY -> balance.scale(terms.rateBp.toLong() * today.daysUntil(terms.endDate), BASIS_POINTS * DAYS_IN_YEAR)
            DepositPayout.CAPITALIZED -> capitalized(terms, balance, today)
        }
        return Money(income.amountMinor, balance.currency)
    }

    /** Whether the closing day is within [days] days from [today] (today included): the line in «Скоро». */
    fun endsWithin(terms: DepositTerms, today: LocalDate, days: Int): Boolean = terms.endDate >= today && today.daysUntil(terms.endDate) < days

    private fun capitalized(terms: DepositTerms, balance: Money, today: LocalDate): Money {
        val months = today.monthsUntil(terms.endDate)
        var total = balance
        repeat(months) { total = total + total.scale(terms.rateBp.toLong(), BASIS_POINTS * MONTHS_IN_YEAR) }
        val rest = today.plus(DatePeriod(months = months)).daysUntil(terms.endDate)
        total = total + total.scale(terms.rateBp.toLong() * rest, BASIS_POINTS * DAYS_IN_YEAR)
        return total - balance
    }

    private const val BASIS_POINTS = 10_000L
    private const val DAYS_IN_YEAR = 365L
    private const val MONTHS_IN_YEAR = 12L
}
