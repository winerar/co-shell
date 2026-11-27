package app.coshell.core.domain.search

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class DuplicateFinderTest {
    private val ars = CurrencyCode("ARS")
    private val now = Instant.parse("2026-11-27T15:00:00Z")

    private fun tx(id: String, minutesAgo: Int, amount: Long = 18_300_00, category: String? = "food", account: String = "lemon", type: TransactionType = TransactionType.EXPENSE, imported: Boolean = false) =
        Transaction(
            id = id, type = type, accountId = account, amount = Money(amount, ars), date = now - minutesAgo.minutes, categoryId = category,
            importSource = if (imported) "wallet" else null, importExternalId = if (imported) id else null,
            toAccountId = "other".takeIf { type == TransactionType.TRANSFER }, toAmount = Money(amount, ars).takeIf { type == TransactionType.TRANSFER },
        )

    @Test
    fun `the same amount and category within ten minutes is a pair, the newer one second`() {
        val pairs = duplicatePairs(listOf(tx("b", 60), tx("a", 63), tx("c", 30, amount = 2_450_00)), emptySet(), now)
        assertEquals(DuplicatePair("a", "b"), pairs["a"])
        assertEquals(DuplicatePair("a", "b"), pairs["b"])
        assertTrue("c" !in pairs)
    }

    @Test
    fun `different category, far apart, transfers, imports and old records are not pairs`() {
        assertTrue(duplicatePairs(listOf(tx("a", 60), tx("b", 61, category = "cafe")), emptySet(), now).isEmpty())
        assertTrue(duplicatePairs(listOf(tx("a", 60), tx("b", 75)), emptySet(), now).isEmpty())
        assertTrue(duplicatePairs(listOf(tx("a", 60, type = TransactionType.TRANSFER, category = null), tx("b", 61, type = TransactionType.TRANSFER, category = null)), emptySet(), now).isEmpty())
        assertTrue(duplicatePairs(listOf(tx("a", 60, imported = true), tx("b", 61)), emptySet(), now).isEmpty())
        val old = (31.days.inWholeMinutes).toInt()
        assertTrue(duplicatePairs(listOf(tx("a", old), tx("b", old + 1)), emptySet(), now).isEmpty())
    }

    @Test
    fun `without a category the same account counts, a dismissed pair stays quiet`() {
        val pairs = duplicatePairs(listOf(tx("a", 60, category = null), tx("b", 61, category = null)), emptySet(), now)
        assertEquals(DuplicatePair("b", "a"), pairs["a"])
        assertTrue(duplicatePairs(listOf(tx("a", 60), tx("b", 61)), setOf(DuplicatePair("a", "b").key), now).isEmpty())
    }

    @Test
    fun `three alike make one pair and leave the third alone`() {
        val pairs = duplicatePairs(listOf(tx("a", 60), tx("b", 59), tx("c", 58)), emptySet(), now)
        assertEquals(setOf("a", "b"), pairs.keys)
    }
}
