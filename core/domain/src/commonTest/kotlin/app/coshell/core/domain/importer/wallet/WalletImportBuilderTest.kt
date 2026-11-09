package app.coshell.core.domain.importer.wallet

import app.coshell.core.domain.importer.wallet.WalletSamples.HEADER
import app.coshell.core.domain.importer.wallet.WalletSamples.SAMPLE
import app.coshell.core.domain.importer.wallet.WalletSamples.row
import app.coshell.core.model.Account
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.TimeZone

class WalletImportBuilderTest {
    private val usd = CurrencyCode("USD")
    private val amd = CurrencyCode("AMD")
    private val ars = CurrencyCode("ARS")
    private val rub = CurrencyCode("RUB")
    private var n = 0
    private val builder = WalletImportBuilder { "t${n++}" }

    private val accounts = mapOf(
        "Карта USD" to Account("usd", "Карта USD", usd),
        "Наличные ARS" to Account("ars", "Наличные ARS", ars),
        "Карта RUB" to Account("rub", "Карта RUB", rub),
        "Счёт AMD" to Account("amd", "Счёт AMD", amd),
    )

    private fun decisions(records: List<WalletRecord>, unpaired: Map<Int, UnpairedChoice> = emptyMap()) = WalletImportDecisions(
        accounts = accounts,
        categories = WalletCategoryMatcher.keys(records).associateWith { "${it.name}/${it.type}" },
        adjustmentCategories = mapOf(CategoryType.EXPENSE to "adj-", CategoryType.INCOME to "adj+"),
        baseCurrency = WalletBaseCurrency.detect(records) ?: amd,
        timeZone = TimeZone.UTC,
        unpaired = unpaired,
    )

    private fun parse(vararg lines: String) = WalletCsvParser.parse((listOf(HEADER) + lines).joinToString("\n")).records

    @Test
    fun sampleBecomesTransactions() {
        val records = WalletCsvParser.parse(SAMPLE).records
        val result = builder.build(records, decisions(records))
        assertEquals(emptyList(), result.issues)
        // 12 rows: 7 plain, 2 pairs, 1 unpaired.
        assertEquals(10, result.transactions.size)

        val bakery = result.transactions.single { it.labels == listOf("Пекарня") }
        assertEquals(TransactionType.EXPENSE, bakery.type)
        assertEquals(Money(1_200_000, ars), bakery.amount)
        assertEquals("Продукты/EXPENSE", bakery.categoryId)
        assertEquals("wallet", bakery.importSource)

        val exchange = result.transactions.single { it.type == TransactionType.TRANSFER && it.accountId == "amd" }
        assertEquals(Money(38_000_000, amd), exchange.amount)
        assertEquals("usd", exchange.toAccountId)
        assertEquals(Money(100_000, usd), exchange.toAmount)

        val unpaired = result.transactions.single { it.categoryId == "adj-" }
        assertEquals(Money(2_000, usd), unpaired.amount)
        assertEquals(result.transactions.size, result.transactions.map { it.importExternalId }.toSet().size)
    }

    @Test
    fun recordInAForeignCurrencyUsesTheBaseAmount() {
        val records = parse(
            row("account" to "Счёт AMD", "currency" to "AMD", "amount" to "-100.00", "ref_currency_amount" to "-100.00"),
            row("account" to "Счёт AMD", "category" to "TRANSFER", "currency" to "RUB", "amount" to "-4675.00",
                "ref_currency_amount" to "-22150.10", "transfer" to "true", "date" to "2026-04-16 03:11:07"),
            row("account" to "Карта RUB", "category" to "TRANSFER", "currency" to "RUB", "amount" to "4675.00",
                "ref_currency_amount" to "22150.10", "transfer" to "true", "date" to "2026-04-16 03:11:07"),
            row("account" to "Счёт AMD", "category" to "Еда", "currency" to "RUB", "amount" to "-10.00", "ref_currency_amount" to "-47.00"),
        )
        val result = builder.build(records, decisions(records))
        val transfer = result.transactions.single { it.type == TransactionType.TRANSFER }
        assertEquals(Money(2_215_010, amd), transfer.amount)
        assertEquals(Money(467_500, rub), transfer.toAmount)
        assertNull(transfer.originalAmount)

        val food = result.transactions.single { it.originalAmount != null }
        assertEquals(Money(4_700, amd), food.amount)
        assertEquals(Money(1_000, rub), food.originalAmount)
    }

    @Test
    fun foreignCurrencyOnANonBaseAccountNeedsARate() {
        val records = parse(row("account" to "Карта USD", "currency" to "EUR", "amount" to "-1.00", "ref_currency_amount" to "-450.00"))
        val result = builder.build(records, decisions(records).copy(baseCurrency = amd))
        assertEquals(listOf(WalletImportIssue(2, WalletImportProblem.NEEDS_RATE)), result.issues)
        assertEquals(emptyList(), result.transactions)
    }

    @Test
    fun unpairedTransferChoices() {
        val records = parse(
            row("account" to "Карта USD", "category" to "TRANSFER", "amount" to "-20.00", "ref_currency_amount" to "-7600.00", "transfer" to "true"),
            row("account" to "Счёт AMD", "category" to "TRANSFER", "currency" to "AMD", "amount" to "500.00",
                "ref_currency_amount" to "500.00", "transfer" to "true", "date" to "2026-11-02 10:00:00"),
        )
        val asTransfer = builder.build(records, decisions(records, mapOf(2 to UnpairedChoice.TransferWith("amd"), 3 to UnpairedChoice.Skip)))
        assertEquals(1, asTransfer.skipped)
        val t = asTransfer.transactions.single()
        assertEquals("usd", t.accountId)
        assertEquals(Money(2_000, usd), t.amount)
        assertEquals("amd", t.toAccountId)
        assertEquals(Money(760_000, amd), t.toAmount) // from ref: AMD is the base

        val adjusted = builder.build(records, decisions(records))
        assertEquals(setOf("adj-", "adj+"), adjusted.transactions.map { it.categoryId }.toSet())
    }

    @Test
    fun unmappedAccountIsReported() {
        val records = parse(row("account" to "Неизвестный"))
        assertEquals(listOf(WalletImportIssue(2, WalletImportProblem.UNMAPPED)), builder.build(records, decisions(records)).issues)
    }
}
