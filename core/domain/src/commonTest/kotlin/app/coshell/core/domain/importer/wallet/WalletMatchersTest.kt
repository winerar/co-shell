package app.coshell.core.domain.importer.wallet

import app.coshell.core.domain.importer.wallet.WalletSamples.HEADER
import app.coshell.core.domain.importer.wallet.WalletSamples.SAMPLE
import app.coshell.core.domain.importer.wallet.WalletSamples.row
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class WalletMatchersTest {
    private val usd = CurrencyCode("USD")
    private val amd = CurrencyCode("AMD")
    private val rub = CurrencyCode("RUB")
    private val sample = WalletCsvParser.parse(SAMPLE).records

    private fun parse(vararg lines: String) = WalletCsvParser.parse((listOf(HEADER) + lines).joinToString("\n")).records

    @Test
    fun accountCurrencyIsTheMostFrequentOne() {
        val records = parse(
            row("account" to "Арм. карты", "currency" to "AMD", "amount" to "-100.00", "ref_currency_amount" to "-100.00"),
            row("account" to "Арм. карты", "currency" to "AMD", "amount" to "-200.00", "ref_currency_amount" to "-200.00"),
            row("account" to "Арм. карты", "currency" to "RUB", "amount" to "-10.00", "ref_currency_amount" to "-47.00"),
        )
        assertEquals(listOf(WalletAccountInfo("Арм. карты", amd, 3)), WalletAccountMatcher.accounts(records))
    }

    @Test
    fun accountsMergeOnlyByNameAndCurrency() {
        val accounts = WalletAccountMatcher.accounts(sample)
        val existing = listOf(
            Account("u", " карта usd ", usd),
            Account("r", "Карта RUB", amd), // same name, other currency
            Account("old", "Счёт AMD", amd, archived = true),
        )
        val choices = WalletAccountMatcher.suggest(accounts, existing)
        assertEquals(AccountChoice.MergeInto("u"), choices["Карта USD"])
        assertEquals(AccountChoice.CreateNew("Карта RUB", rub), choices["Карта RUB"])
        assertEquals(AccountChoice.CreateNew("Счёт AMD", amd), choices["Счёт AMD"])
    }

    @Test
    fun categoryWithBothDirectionsGivesTwoKeys() {
        val records = parse(
            row("category" to "Подарки", "amount" to "-1.00"),
            row("category" to "Подарки", "amount" to "2.00", "ref_currency_amount" to "760.00"),
            row("category" to "TRANSFER", "transfer" to "true"),
        )
        assertEquals(
            setOf(WalletCategoryKey("Подарки", CategoryType.EXPENSE), WalletCategoryKey("Подарки", CategoryType.INCOME)),
            WalletCategoryMatcher.keys(records).toSet(),
        )
    }

    @Test
    fun categoriesMatchByNameAndType() {
        val existing = listOf(
            Category("food", "продукты", CategoryType.EXPENSE),
            Category("salary", "Зарплата", CategoryType.EXPENSE), // wrong type
        )
        val choices = WalletCategoryMatcher.suggest(WalletCategoryMatcher.keys(sample), existing)
        assertEquals(CategoryChoice.UseExisting("food"), choices[WalletCategoryKey("Продукты", CategoryType.EXPENSE)])
        assertEquals(
            CategoryChoice.CreateNew("Зарплата", CategoryType.INCOME),
            choices[WalletCategoryKey("Зарплата", CategoryType.INCOME)],
        )
    }

    @Test
    fun baseCurrencyIsTheOneWithEqualRefAmounts() {
        assertEquals(amd, WalletBaseCurrency.detect(sample))
    }

    @Test
    fun externalIdsSurviveANewerExport() {
        val old = parse(
            row("amount" to "-1.00", "date" to "2026-10-01 10:00:00"),
            row("amount" to "-1.00", "date" to "2026-10-01 10:00:00"), // identical twin
        )
        val newer = parse(row("amount" to "-5.00", "date" to "2026-11-01 10:00:00")) + old
        val oldIds = WalletExternalIds.of(old).values.toSet()
        val newIds = WalletExternalIds.of(newer).values.toSet()
        assertEquals(2, oldIds.size)
        assertEquals(3, newIds.size)
        assertEquals(oldIds, newIds intersect oldIds)
        assertNotEquals(WalletExternalIds.fnv1a64("a"), WalletExternalIds.fnv1a64("b"))
    }
}
