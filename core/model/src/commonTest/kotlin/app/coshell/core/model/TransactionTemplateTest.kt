package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull

class TransactionTemplateTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")

    private fun template(
        name: String = "Испанский",
        type: TransactionType = TransactionType.EXPENSE,
        amount: Money? = Money(40_000_00, ars),
        toAccountId: String? = null,
    ) = TransactionTemplate("t", name, type, "galicia", ars, amount, toAccountId = toAccountId)

    @Test
    fun `amount is optional`() {
        assertNull(template(amount = null).amount)
    }

    @Test
    fun `name must not be blank`() {
        assertFailsWith<IllegalArgumentException> { template(name = "  ") }
    }

    @Test
    fun `amount must be positive`() {
        assertFailsWith<IllegalArgumentException> { template(amount = Money(0, ars)) }
    }

    @Test
    fun `an expense may be in another currency, a transfer may not`() {
        assertTrue(template(amount = Money(650_00, usd)).isForeign)
        assertFalse(template().isForeign)
        assertFalse(template(amount = null).isForeign)
        assertFailsWith<IllegalArgumentException> {
            template(type = TransactionType.TRANSFER, amount = Money(650_00, usd), toAccountId = "cash")
        }
    }

    @Test
    fun `transfer needs another account, other types must not have one`() {
        template(type = TransactionType.TRANSFER, toAccountId = "cash")
        assertFailsWith<IllegalArgumentException> { template(type = TransactionType.TRANSFER) }
        assertFailsWith<IllegalArgumentException> { template(type = TransactionType.TRANSFER, toAccountId = "galicia") }
        assertFailsWith<IllegalArgumentException> { template(toAccountId = "cash") }
    }
}
