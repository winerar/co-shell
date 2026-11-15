package app.coshell.core.domain.recurring

import app.coshell.core.domain.FakeTransactionRepository
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class PaymentHistoryTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")

    private fun paid(template: String, day: String, amount: Long, currency: CurrencyCode = ars, id: String = autoPaymentId(template, LocalDate.parse(day))) =
        Transaction(id, TransactionType.EXPENSE, "acc", Money(amount, currency), Instant.parse("${day}T12:00:00Z"), categoryId = "home")

    @Test
    fun `nothing paid yet - no hint`() {
        assertNull(paymentHistory("light", listOf(paid("water", "2026-10-15", 100))))
    }

    @Test
    fun `one payment - last without average`() {
        assertEquals(PaymentHistory(Money(41_300_00, ars), null), paymentHistory("light", listOf(paid("light", "2026-10-15", 41_300_00))))
    }

    @Test
    fun `last is the newest, average over the latest six in its currency`() {
        val list = listOf(
            paid("light", "2026-04-15", 99_000_00), // seventh from the end: out of the average
            paid("light", "2026-05-15", 40_000_00),
            paid("light", "2026-06-15", 30, usd), // the account was in dollars then
            paid("light", "2026-07-15", 44_000_00),
            paid("light", "2026-08-15", 46_000_00),
            paid("light", "2026-09-15", 42_700_00),
            paid("light", "2026-11-15", 41_300_00),
            paid("light", "2026-10-15", 50_000_00),
        )
        val history = paymentHistory("light", list.shuffled())!!
        assertEquals(Money(41_300_00, ars), history.last)
        assertEquals(Money(44_000_00, ars), history.average)
    }

    @Test
    fun `ordinary operations and payments of a template with a longer id do not count`() {
        val list = listOf(
            paid("light", "2026-10-15", 41_300_00),
            paid("light-2", "2026-11-01", 1_00),
            paid("light", "2026-11-02", 2_00, id = "random-id"),
        )
        assertEquals(PaymentHistory(Money(41_300_00, ars), null), paymentHistory("light", list))
    }

    @Test
    fun `the use case reads payments of the template`() = runTest {
        val repo = FakeTransactionRepository()
        repo.saveTransaction(paid("light", "2026-09-15", 40_000_00))
        repo.saveTransaction(paid("light", "2026-10-15", 42_000_00))
        val template = TransactionTemplate(
            "light", "Свет", TransactionType.EXPENSE, "acc", ars, categoryId = "home",
            schedule = RecurringSchedule(Recurrence.Monthly(15), LocalDate.parse("2026-01-01"), PostMode.REMIND, null),
        )
        val history = PaymentHistoryUseCase(repo)(template, LocalDate.parse("2026-11-15"), TimeZone.UTC)
        assertEquals(PaymentHistory(Money(42_000_00, ars), Money(41_000_00, ars)), history)
    }
}
