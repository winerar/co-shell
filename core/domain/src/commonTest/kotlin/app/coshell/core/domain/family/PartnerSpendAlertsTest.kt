package app.coshell.core.domain.family

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class PartnerSpendAlertsTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val now = Instant.parse("2026-11-13T21:47:00Z")
    private val threshold = Money.ofMajor(50_000, currency = ars)

    private fun op(
        id: String,
        major: Long,
        currency: CurrencyCode = ars,
        author: String? = "anya",
        type: TransactionType = TransactionType.EXPENSE,
        date: Instant = now - 1.hours,
        categoryId: String? = "tech",
    ) = Transaction(
        id = id, type = type, accountId = "galicia", amount = Money.ofMajor(major, currency = currency), date = date, categoryId = categoryId,
        authorMemberId = author,
        toAccountId = if (type == TransactionType.TRANSFER) "cash" else null,
        toAmount = if (type == TransactionType.TRANSFER) Money.ofMajor(major, currency = currency) else null,
    )

    /** 1 USD = 1 000 ARS. */
    private val toArs: (Transaction) -> Money? = { t -> if (t.amount.currency == usd) Money(t.amount.amountMinor * 1_000, ars) else null }

    private fun pick(vararg ops: Transaction, notified: Set<String> = emptySet(), convert: (Transaction) -> Money? = toArs) =
        PartnerSpendAlerts.pick(ops.toList(), me = "misha", threshold = threshold, now = now, alreadyNotified = notified, convert = convert)

    @Test
    fun `partner expense at or above the threshold is reported`() {
        val alerts = pick(op("a", 80_000), op("b", 50_000), op("c", 49_999))
        assertEquals(listOf("a", "b"), alerts.map { it.transaction.id })
        assertEquals(Money.ofMajor(80_000, currency = ars), alerts[0].inThreshold)
    }

    @Test
    fun `other currency is compared at the converted amount`() {
        val alerts = pick(op("usd-big", 60, usd), op("usd-small", 40, usd))
        assertEquals(listOf("usd-big"), alerts.map { it.transaction.id })
        assertEquals(Money.ofMajor(60_000, currency = ars), alerts.single().inThreshold)
    }

    @Test
    fun `no rate means silence`() {
        assertTrue(pick(op("usd", 500, usd), convert = { null }).isEmpty())
    }

    @Test
    fun `own, unknown author, income, transfer and adjustment are not reported`() {
        val alerts = pick(
            op("mine", 90_000, author = "misha"),
            op("nobody", 90_000, author = null),
            op("income", 90_000, type = TransactionType.INCOME),
            op("transfer", 90_000, type = TransactionType.TRANSFER, categoryId = null),
            op("adjust", 90_000, categoryId = SystemCategories.ADJUSTMENT_EXPENSE_ID),
        )
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `imported operations are not reported`() {
        val imported = op("w", 90_000).copy(importSource = "wallet", importExternalId = "x1")
        assertTrue(pick(imported).isEmpty())
    }

    @Test
    fun `old operation and already notified are skipped`() {
        assertTrue(pick(op("old", 90_000, date = now - 2.days), op("seen", 90_000), notified = setOf("seen")).isEmpty())
    }

    @Test
    fun `alerts come in time order`() {
        val alerts = pick(op("late", 70_000, date = now - 1.hours), op("early", 70_000, date = now - 5.hours))
        assertEquals(listOf("early", "late"), alerts.map { it.transaction.id })
    }
}
