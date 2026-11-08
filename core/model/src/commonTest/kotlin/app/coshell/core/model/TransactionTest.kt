package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Instant

class TransactionTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val now = Instant.parse("2026-11-08T12:00:00Z")

    private fun expense(amount: Long = 100) =
        Transaction("t1", TransactionType.EXPENSE, "a", Money(amount, usd), now)

    @Test
    fun `requires a positive amount`() {
        assertFailsWith<IllegalArgumentException> { expense(0) }
        assertFailsWith<IllegalArgumentException> { expense(-5) }
    }

    @Test
    fun `transfer needs a different target account and a target amount`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction("t", TransactionType.TRANSFER, "a", Money(1, usd), now, toAccountId = "a", toAmount = Money(1, usd))
        }
        assertFailsWith<IllegalArgumentException> {
            Transaction("t", TransactionType.TRANSFER, "a", Money(1, usd), now, toAccountId = "b")
        }
    }

    @Test
    fun `only transfers have a target account`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction("t", TransactionType.EXPENSE, "a", Money(1, usd), now, toAccountId = "b", toAmount = Money(1, usd))
        }
    }

    @Test
    fun `effect on balances follows the type`() {
        assertEquals(Money(-100, usd), expense().effectOn("a"))
        assertNull(expense().effectOn("b"))
        val exchange = Transaction(
            "t", TransactionType.TRANSFER, "a", Money(100_00, usd), now,
            toAccountId = "b", toAmount = Money(156_800_000, ars),
        )
        assertEquals(Money(-100_00, usd), exchange.effectOn("a"))
        assertEquals(Money(156_800_000, ars), exchange.effectOn("b"))
    }

    @Test
    fun `import source and external id go together`() {
        assertFailsWith<IllegalArgumentException> {
            Transaction("t", TransactionType.INCOME, "a", Money(1, usd), now, importSource = "wallet")
        }
    }
}
