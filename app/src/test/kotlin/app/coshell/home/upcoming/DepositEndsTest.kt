package app.coshell.home.upcoming

import app.coshell.core.domain.debts.DebtBook
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.DepositPayout
import app.coshell.core.model.DepositTerms
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/** «Скоро» lines of deposits (#634, step 3) and of debts (#552): who is listed and what the line says. */
class DepositEndsTest {
    private val usd = CurrencyCode("USD")
    private val today = LocalDate(2026, 3, 15)

    private fun deposit(id: String, end: LocalDate, balance: Long = 10_000_00, archived: Boolean = false) = AccountWithBalance(
        Account(id, "Вклад $id", usd, AccountType.DEPOSIT, archived = archived, deposit = DepositTerms(800, end, DepositPayout.AT_END)),
        Money(balance, usd),
    )

    @Test
    fun `only deposits closing within the window with a balance are listed, soonest first`() {
        val rows = buildDepositRows(
            listOf(
                deposit("later", LocalDate(2026, 3, 30)),
                deposit("tomorrow", LocalDate(2026, 3, 16)),
                deposit("today", today),
                deposit("empty", LocalDate(2026, 3, 16), balance = 0),
                deposit("gone", LocalDate(2026, 3, 16), archived = true),
                deposit("past", LocalDate(2026, 3, 1)),
            ),
            MoneyFormat.RU, today, 7,
        )
        assertEquals(listOf("today", "tomorrow"), rows.map { it.accountId })
    }

    @Test
    fun `the line carries the expected income of the days left`() {
        val row = buildDepositRows(listOf(deposit("a", LocalDate(2026, 3, 21))), MoneyFormat.RU, today, 7).single()
        // 10 000 × 8 % × 6 / 365 = 13.15
        assertEquals("13,15" + MoneyFormat.NBSP + "USD", row.income)
    }

    @Test
    fun `debts show up as lines with their stage`() {
        assertEquals(emptyList<DebtDueRow>(), buildDebtRows(DebtBook.due(emptyList(), today), MoneyFormat.RU))
    }
}
