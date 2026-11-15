package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.rates
import app.coshell.core.domain.budgets.BudgetFixtures.today
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccountsAtTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val book = RateBook(rates) // USD→ARS: 1 000 from 1 Sep, 2 000 from 5 Nov
    private fun usd(units: Long) = Money(units * 100, usd)

    private val cash = Account("cash", "Доллары", usd, initialBalanceMinor = 1_000_00)
    private val peso = Account("peso", "Песо", ars, initialBalanceMinor = 1_000_000_00)
    private val debt = Account("debt", "Долг", usd, initialBalanceMinor = -500_00, excludedFromTotal = true)
    private val october = LocalDate(2026, 10, 31)
    private val september = LocalDate(2026, 9, 30)

    private fun d(month: Int, day: Int, account: String, minor: Long) = BalanceDay(LocalDate(2026, month, day), account, minor)

    @Test
    fun `balances at the day in their currency and the report one, change since the month before`() {
        val rows = accountsAt(listOf(cash, peso, debt), listOf(d(10, 3, "cash", 200_00), d(11, 2, "cash", -100_00)), book, usd, october, september)

        assertEquals(listOf("cash", "peso"), rows.map { it.account.id })
        assertEquals(usd(1_200), rows[0].balance)
        assertEquals(usd(1_200), rows[0].converted)
        assertEquals(usd(200), rows[0].change)
        assertEquals(Money(1_000_000_00, ars), rows[1].balance)
        assertEquals(usd(1_000), rows[1].converted)
        assertEquals(usd(0), rows[1].change)
    }

    @Test
    fun `rates moving show as change even with the same balance`() {
        val rows = accountsAt(listOf(peso), emptyList(), book, usd, today, october)

        assertEquals(usd(500), rows.single().converted)
        assertEquals(usd(-500), rows.single().change)
    }

    @Test
    fun `an account emptied this month stays, one empty on both days is left out`() {
        val spent = Account("spent", "Потратили", usd)
        val empty = Account("empty", "Пусто", usd)
        val rows = accountsAt(listOf(spent, empty), listOf(d(9, 10, "spent", 300_00), d(10, 10, "spent", -300_00)), book, usd, october, september)

        assertEquals(usd(0), rows.single().balance)
        assertEquals(usd(-300), rows.single().change)
    }

    @Test
    fun `no rate — no converted amount and no change, no earlier point — no change`() {
        val amd = Account("amd", "Драмы", CurrencyCode("AMD"), initialBalanceMinor = 10_000_00)

        assertNull(accountsAt(listOf(amd), emptyList(), book, usd, october, september).single().converted)
        assertNull(accountsAt(listOf(amd), emptyList(), book, usd, october, september).single().change)
        assertNull(accountsAt(listOf(cash), emptyList(), book, usd, october, null).single().change)
    }
}
