package app.coshell.core.domain.credit

import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CreditTerms
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MinPayment
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class CreditPaymentsTest {
    private val amd = CurrencyCode("AMD")
    private val zone = TimeZone.UTC
    private val today = LocalDate(2026, 12, 12)
    private val card = Account("card", "Кредитка", amd, credit = CreditTerms(500_000_00, 15, MinPayment.Percent(500)))
    private fun money(major: Long) = Money(major * 100, amd)
    private fun item(balance: Long, account: Account = card) = AccountWithBalance(account, money(balance))
    private fun payment(day: String, amount: Long, to: String = "card") =
        Transaction("p$day", TransactionType.TRANSFER, "salary", money(amount), Instant.parse("${day}T10:00:00Z"), toAccountId = to, toAmount = money(amount))

    @Test
    fun `a card with a debt shows its payment three days ahead with the minimum`() {
        val due = creditPaymentsDue(listOf(item(-180_000)), emptyList(), today, zone).single()
        assertEquals(LocalDate(2026, 12, 15), due.due)
        assertEquals(3, due.daysLeft)
        assertEquals(money(9_000), due.minimum)
    }

    @Test
    fun `a payment later than the window or a card without a debt is not listed`() {
        assertEquals(emptyList(), creditPaymentsDue(listOf(item(-180_000)), emptyList(), LocalDate(2026, 12, 1), zone))
        assertEquals(emptyList(), creditPaymentsDue(listOf(item(0)), emptyList(), today, zone))
        assertEquals(emptyList(), creditPaymentsDue(listOf(item(-180_000, card.copy(archived = true))), emptyList(), today, zone))
        assertEquals(emptyList(), creditPaymentsDue(listOf(item(-5_000, Account("plain", "Карта", amd))), emptyList(), today, zone))
    }

    @Test
    fun `today counts`() {
        assertEquals(0, creditPaymentsDue(listOf(item(-180_000)), emptyList(), LocalDate(2026, 12, 15), zone).single().daysLeft)
    }

    @Test
    fun `a payment of this cycle that reaches the minimum quiets the card`() {
        val paid = listOf(payment("2026-12-05", 9_000))
        assertEquals(emptyList(), creditPaymentsDue(listOf(item(-171_000)), paid, today, zone))
    }

    @Test
    fun `payments add up, but a smaller sum, an older one or another card does not close it`() {
        val half = listOf(payment("2026-12-05", 3_000), payment("2026-12-08", 6_000))
        assertEquals(emptyList(), creditPaymentsDue(listOf(item(-171_000)), half, today, zone))
        assertEquals(1, creditPaymentsDue(listOf(item(-177_000)), listOf(payment("2026-12-05", 3_000)), today, zone).size)
        assertEquals(1, creditPaymentsDue(listOf(item(-171_000)), listOf(payment("2026-11-15", 9_000)), today, zone).size)
        assertEquals(1, creditPaymentsDue(listOf(item(-171_000)), listOf(payment("2026-12-05", 9_000, to = "other")), today, zone).size)
    }
}
