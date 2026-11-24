package app.coshell.accounts.list

import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CreditTerms
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MinPayment
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.testing.FakeAccountRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** A credit card in «Счета» (#686): what is left of the limit and the payment day. */
class AccountsCreditTest {
    private val amd = CurrencyCode("AMD")
    private val terms = CreditTerms(500_000_00, 15, MinPayment.Percent(500))
    private val clock = object : Clock { override fun now(): Instant = Instant.parse("2026-11-23T12:00:00Z") }
    private val vm = AccountsViewModel(FakeAccountRepository(), MoneyFormat.RU, clock = clock, timeZone = TimeZone.UTC)

    private fun row(balance: Long, credit: CreditTerms? = terms) =
        vm.buildState(listOf(AccountWithBalance(Account("c", "Кредитка", amd, credit = credit), Money(balance, amd))), showArchived = false).active.single()

    @Test
    fun `a card with a debt shows what is available, how much is spent and the next payment day`() {
        val credit = row(-180_000_00).credit!!
        assertEquals(MoneyFormat.RU.format(Money(320_000_00, amd)), credit.available)
        assertEquals(MoneyFormat.RU.format(Money(500_000_00, amd)), credit.limit)
        assertEquals(0.36f, credit.used, 0.0001f)
        assertEquals(LocalDate(2026, 12, 15), credit.due)
    }

    @Test
    fun `a card without a debt has no payment day and an empty meter`() {
        val credit = row(0).credit!!
        assertNull(credit.due)
        assertEquals(0f, credit.used, 0f)
    }

    @Test
    fun `an ordinary account has no credit row`() {
        assertNull(row(-10_00, credit = null).credit)
    }
}
