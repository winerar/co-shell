package app.coshell.core.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class TransactionTest {
    private val rub = CurrencyCode("RUB")
    private val eur = CurrencyCode("EUR")
    private val card = AccountId("card")
    private val cash = AccountId("cash")
    private val meta = SyncMeta(updatedAt = Instant.fromEpochSeconds(0))
    private val date = LocalDate(2026, 11, 8)

    private fun expense(amount: Money = Money(1000, rub), meta: SyncMeta = this.meta) = Transaction(
        id = TransactionId("t"), type = TransactionType.EXPENSE, accountId = card,
        amount = amount, date = date, meta = meta,
    )

    private fun transfer(toAccount: AccountId = cash, toAmount: Money? = Money(100, eur)) = Transaction(
        id = TransactionId("t"), type = TransactionType.TRANSFER, accountId = card,
        amount = Money(10_000, rub), toAccountId = toAccount, toAmount = toAmount,
        date = date, meta = meta,
    )

    @Test
    fun `rejects zero and negative amounts`() {
        assertFailsWith<IllegalArgumentException> { expense(Money(0, rub)) }
        assertFailsWith<IllegalArgumentException> { expense(Money(-5, rub)) }
    }

    @Test
    fun `transfer needs a different target account and amount`() {
        assertFailsWith<IllegalArgumentException> { transfer(toAccount = card) }
        assertFailsWith<IllegalArgumentException> { transfer(toAmount = null) }
    }

    @Test
    fun `expense cannot have a target account`() {
        assertFailsWith<IllegalArgumentException> { expense().copy(toAccountId = cash) }
    }

    @Test
    fun `original amount must be in another currency`() {
        assertFailsWith<IllegalArgumentException> { expense().copy(originalAmount = Money(5, rub)) }
        assertEquals(Money(12, eur), expense().copy(originalAmount = Money(12, eur)).originalAmount)
    }

    @Test
    fun `import source and external id go together`() {
        assertFailsWith<IllegalArgumentException> { expense().copy(importSource = "wallet") }
    }

    @Test
    fun `computes signed effect on accounts`() {
        assertEquals(-1000, expense().effectOn(card))
        assertEquals(0, expense().effectOn(cash))
        assertEquals(1000, expense().copy(type = TransactionType.INCOME).effectOn(card))
        assertEquals(-10_000, transfer().effectOn(card))
        assertEquals(100, transfer().effectOn(cash))
    }

    @Test
    fun `deleted transaction has no effect`() {
        assertEquals(0, expense(meta = meta.copy(deleted = true)).effectOn(card))
    }
}
