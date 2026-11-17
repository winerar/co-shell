package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.rates
import app.coshell.core.domain.budgets.BudgetFixtures.usd
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaidWithTest {
    private val ars = CurrencyCode("ARS")
    private val usdt = CurrencyCode("USDT")

    private fun d(day: Int, account: String, category: String, amount: Money, type: TransactionType = TransactionType.EXPENSE) =
        ReportAccountDay(LocalDate(2026, 11, day), account, category, type, amount)

    private fun paid(vararg days: ReportAccountDay, currency: CurrencyCode = ars) =
        paidWith(days.toList(), categories, RateBook(rates), currency)

    @Test
    fun `by account and by currency, largest first, shares add up to 100`() {
        val result = paid(
            d(2, "card", "cafe", ars(1_000)),
            d(3, "card", "rent", ars(500)),
            d(6, "dollars", "cafe", usd(1)),
            d(7, "cash", "cafe", ars(500)),
        )

        // 5 Nov: 1 USD = 2000 ARS
        assertEquals(ars(4_000), result.total)
        assertEquals(listOf("dollars", "card", "cash"), result.byAccount.map { it.accountId })
        assertEquals(listOf(usd(1), ars(1_500), ars(500)), result.byAccount.map { it.native })
        assertEquals(listOf(ars(2_000), ars(1_500), ars(500)), result.byAccount.map { it.converted })
        assertEquals(100, result.byAccount.sumOf { it.percent })
        assertEquals(50, result.byAccount.first().percent)
        assertEquals(listOf(ars, CurrencyCode("USD")), result.byCurrency.map { it.currency }, "a tie — by code")
        assertEquals(listOf(ars(2_000), usd(1)), result.byCurrency.map { it.native })
        assertEquals(listOf(50, 50), result.byCurrency.map { it.percent })
    }

    @Test
    fun `refunds reduce, income, adjustments and an account that got more back are not spending`() {
        val result = paid(
            d(2, "card", "cafe", ars(1_000)),
            d(3, "card", "cafe", ars(400), TransactionType.INCOME),
            d(4, "card", "salary", ars(900_000), TransactionType.INCOME),
            d(5, "card", SystemCategories.ADJUSTMENT_EXPENSE_ID, ars(70)),
            d(6, "other", "cafe", ars(100)),
            d(7, "other", "cafe", ars(300), TransactionType.INCOME),
        )

        assertEquals(listOf("card"), result.byAccount.map { it.accountId })
        assertEquals(ars(600), result.total)
        assertEquals(listOf(100), result.byAccount.map { it.percent })
    }

    @Test
    fun `a day without a rate is named and left out`() {
        val result = paid(d(2, "card", "cafe", ars(10)), d(3, "usdt", "cafe", Money(1_000, usdt)))

        assertEquals(setOf(usdt), result.missing)
        assertEquals(listOf("card"), result.byAccount.map { it.accountId })
    }

    @Test
    fun `no spending — empty`() {
        assertTrue(paid().isEmpty)
        assertEquals(ars(0), paid().total)
    }
}
