package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.time.Instant

class EntityTest {
    private val meta = SyncMeta(updatedAt = Instant.fromEpochSeconds(0))

    @Test
    fun `random ids are unique`() {
        assertNotEquals(AccountId.random(), AccountId.random())
    }

    @Test
    fun `account exposes initial balance as money`() {
        val account = Account(
            id = AccountId("a"), name = "Карта", currency = CurrencyCode("RUB"),
            type = AccountType.CARD, initialBalanceMinor = -500, meta = meta,
        )
        assertEquals(Money(-500, CurrencyCode("RUB")), account.initialBalance)
    }

    @Test
    fun `rejects blank names and self parent`() {
        assertFailsWith<IllegalArgumentException> {
            Account(AccountId("a"), " ", CurrencyCode("RUB"), AccountType.CASH, meta = meta)
        }
        assertFailsWith<IllegalArgumentException> {
            Category(CategoryId("c"), "Еда", CategoryType.EXPENSE, parentId = CategoryId("c"), meta = meta)
        }
    }
}
