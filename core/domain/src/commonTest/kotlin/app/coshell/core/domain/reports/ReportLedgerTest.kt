package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.BudgetFixtures.rates
import app.coshell.core.domain.budgets.BudgetFixtures.usd
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

internal fun day(month: Int, day: Int, categoryId: String, amount: Money, type: TransactionType = TransactionType.EXPENSE) =
    ReportDay(LocalDate(2026, month, day), categoryId, type, amount)

internal fun ledger(vararg days: ReportDay, currency: CurrencyCode = CurrencyCode("ARS")) =
    ReportLedger(days.toList(), categories, RateBook(rates), currency)

class ReportLedgerTest {
    private val period = budgetPeriod(november)

    @Test
    fun `refunds reduce spending, income goes apart, adjustments count nowhere`() {
        val ledger = ledger(
            day(11, 2, "cafe", ars(10_000)),
            day(11, 3, "cafe", ars(4_000), TransactionType.INCOME),
            day(11, 4, "salary", ars(900_000), TransactionType.INCOME),
            day(11, 5, SystemCategories.ADJUSTMENT_EXPENSE_ID, ars(50_000)),
            day(11, 5, SystemCategories.ADJUSTMENT_INCOME_ID, ars(70_000), TransactionType.INCOME),
        )

        assertEquals(CategorySums(mapOf("cafe" to ars(6_000)), emptySet()), ledger.spent(period))
        assertEquals(CategorySums(mapOf("salary" to ars(900_000)), emptySet()), ledger.earned(period))
    }

    @Test
    fun `each day is converted at its own rate, days without a rate are named`() {
        val ledger = ledger(
            day(11, 4, "cafe", usd(10)),
            day(11, 5, "cafe", usd(10)),
            day(11, 6, "cafe", Money.ofMajor(5_000, currency = CurrencyCode("AMD"))),
        )

        val spent = ledger.spent(period)

        assertEquals(mapOf("cafe" to ars(30_000)), spent.byCategory)
        assertEquals(setOf(CurrencyCode("AMD")), spent.missing)
    }

    @Test
    fun `a deleted category keeps its operations`() {
        val ledger = ledger(day(11, 2, "gone", ars(1_000)), day(11, 3, "gone-income", ars(2_000), TransactionType.INCOME))

        assertEquals(mapOf("gone" to ars(1_000)), ledger.spent(period).byCategory)
        assertEquals(mapOf("gone-income" to ars(2_000)), ledger.earned(period).byCategory)
    }

    @Test
    fun `only days of the period count`() {
        val ledger = ledger(day(10, 31, "cafe", ars(1)), day(11, 1, "cafe", ars(10)), day(11, 30, "cafe", ars(100)), day(12, 1, "cafe", ars(1_000)))

        assertEquals(ars(110), ledger.spent(period).total(ledger.currency))
    }
}
