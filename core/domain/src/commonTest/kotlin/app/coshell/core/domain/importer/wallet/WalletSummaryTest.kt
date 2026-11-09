package app.coshell.core.domain.importer.wallet

import app.coshell.core.domain.importer.wallet.WalletSamples.HEADER
import app.coshell.core.domain.importer.wallet.WalletSamples.row
import app.coshell.core.model.CurrencyCode
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WalletSummaryTest {

    private fun records(vararg lines: String) =
        WalletCsvParser.parse((listOf(HEADER) + lines).joinToString("\n")).records

    @Test
    fun summarizesTheSample() {
        val summary = WalletSummary.of(WalletCsvParser.parse(WalletSamples.SAMPLE).records)

        assertEquals(12, summary.records)
        assertEquals(LocalDate(2026, 10, 4), summary.firstDate)
        assertEquals(LocalDate(2026, 11, 8), summary.lastDate)
        assertEquals(
            listOf("Карта RUB" to 2, "Карта USD" to 4, "Наличные ARS" to 4, "Счёт AMD" to 2),
            summary.accounts.map { it.name to it.records },
        )
        assertEquals(listOf("AMD", "ARS", "RUB", "USD"), summary.currencies.map { it.value })
        assertEquals(2, summary.pairedTransfers)
        assertEquals(1, summary.unpairedTransfers)
        assertEquals(setOf("Кофе", "Курсы", "Пекарня"), summary.labels)

        val names = summary.categories.map { it.name }
        assertEquals(
            listOf("Дети", "Зарплата", "Образование", "Продукты", "Проценты с вкладов, дивиденды", "Рестораны"),
            names,
        )
        val groceries = summary.categories.single { it.name == "Продукты" }
        assertEquals(WalletCategorySummary("Продукты", expenses = 2, incomes = 0, setOf("1000", "1003")), groceries)
        assertEquals(1, summary.categories.single { it.name == "Зарплата" }.incomes)
    }

    @Test
    fun accountWithTwoCurrenciesKeepsBoth() {
        val summary = WalletSummary.of(
            records(row("account" to "Крипта", "currency" to "USD"), row("account" to "Крипта", "currency" to "USDT")),
        )
        assertEquals(setOf(CurrencyCode("USD"), CurrencyCode("USDT")), summary.accounts.single().currencies)
    }

    @Test
    fun emptyListGivesEmptySummary() {
        val summary = WalletSummary.of(emptyList())
        assertEquals(0, summary.records)
        assertEquals(null, summary.firstDate)
        assertEquals(emptyList(), summary.categories)
    }
}
