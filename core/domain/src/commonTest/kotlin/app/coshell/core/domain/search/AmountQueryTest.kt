package app.coshell.core.domain.search

import app.coshell.core.domain.search.AmountQuery.Kind
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class AmountQueryTest {
    private fun money(minor: Long, code: String) = Money(minor, CurrencyCode(code))

    private fun expense(minor: Long, code: String = "ARS", receipt: Money? = null) = Transaction(
        id = "t",
        type = TransactionType.EXPENSE,
        accountId = "a",
        amount = money(minor, code),
        date = Instant.fromEpochMilliseconds(0),
        originalAmount = receipt,
    )

    @Test
    fun `plain text and plain numbers stay a text search`() {
        assertNull(AmountQuery.parse("кафе"))
        assertNull(AmountQuery.parse("4500"))
        assertNull(AmountQuery.parse("кафе 500"))
        assertNull(AmountQuery.parse("="))
        assertNull(AmountQuery.parse("1-2-3"))
    }

    @Test
    fun `each form is understood`() {
        assertEquals(AmountQuery(450_000, 450_000), AmountQuery.parse("=4500"))
        assertEquals(AmountQuery(1_000_000, null, minInclusive = false), AmountQuery.parse(">10000"))
        assertEquals(AmountQuery(null, 50_000, maxInclusive = false), AmountQuery.parse("< 500"))
        assertEquals(AmountQuery(300_000, 500_000), AmountQuery.parse("3000-5000"))
        assertEquals(AmountQuery(300_000, 500_000), AmountQuery.parse("5 000 – 3 000"))
        assertEquals(AmountQuery(4_120, 4_120), AmountQuery.parse("=41,2"))
        assertEquals(AmountQuery(4_120, null), AmountQuery.parse(">=41.20"))
        assertEquals(Kind.RANGE, AmountQuery.parse("1-2")?.kind)
        assertEquals(Kind.EXACT, AmountQuery.parse("=1")?.kind)
        assertEquals(Kind.MORE, AmountQuery.parse(">1")?.kind)
        assertEquals(Kind.AT_MOST, AmountQuery.parse("<=1")?.kind)
    }

    @Test
    fun `bounds compare in any currency, exactly`() {
        val range = AmountQuery.parse("3000-5000")!!
        assertTrue(range.matches(expense(450_000)))
        assertTrue(range.matches(expense(300_000)))
        assertTrue(range.matches(expense(500_000)))
        assertFalse(range.matches(expense(500_001)))
        assertTrue(range.matches(expense(4_800, "JPY")))
        assertTrue(range.matches(expense(4_000_000, "KWD")))
        assertTrue(range.matches(expense(400_000_000_000, "BTC")))
        assertFalse(AmountQuery.parse(">5000")!!.matches(expense(500_000)))
        assertTrue(AmountQuery.parse(">=5000")!!.matches(expense(500_000)))
    }

    @Test
    fun `the receipt amount counts too`() {
        val paidInUsdt = expense(4_110, "USDT", receipt = money(420_000, "ARS"))

        assertTrue(AmountQuery.parse("=4200")!!.matches(paidInUsdt))
        assertTrue(AmountQuery.parse("=41,10")!!.matches(paidInUsdt))
        assertFalse(AmountQuery.parse("=100")!!.matches(paidInUsdt))
    }

    @Test
    fun `the arriving amount of a transfer counts too`() {
        val exchange = Transaction(
            id = "t", type = TransactionType.TRANSFER, accountId = "a", toAccountId = "b",
            amount = money(10_000, "USD"), toAmount = money(12_000_000, "ARS"), date = Instant.fromEpochMilliseconds(0),
        )

        assertTrue(AmountQuery.parse(">100000")!!.matches(exchange))
        assertTrue(AmountQuery.parse("=100")!!.matches(exchange))
    }
}
