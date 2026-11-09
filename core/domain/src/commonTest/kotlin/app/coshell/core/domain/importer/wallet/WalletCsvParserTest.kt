package app.coshell.core.domain.importer.wallet

import app.coshell.core.domain.importer.wallet.WalletSamples.HEADER
import app.coshell.core.domain.importer.wallet.WalletSamples.row
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WalletCsvParserTest {

    private fun parse(vararg lines: String) = WalletCsvParser.parse(lines.joinToString("\n"))

    @Test
    fun readsEveryRowOfTheSample() {
        val result = WalletCsvParser.parse(WalletSamples.SAMPLE)
        assertEquals(emptyList(), result.errors)
        assertEquals(12, result.records.size)
        assertEquals(
            WalletRecord(
                line = 2,
                account = "Карта USD",
                category = "Образование",
                amount = Money(-1000, CurrencyCode("USD")),
                refAmount = WalletDecimal(-380000, 2),
                note = "",
                dateTime = LocalDateTime(2026, 11, 8, 11, 23, 53),
                isTransfer = false,
                payee = null,
                label = "Курсы",
                envelopeId = "6006",
            ),
            result.records.first(),
        )
        val stroller = result.records[3]
        assertEquals("Коляска: аванс", stroller.note)
        assertEquals(Money(-200000, CurrencyCode("RUB")), stroller.amount)
        assertTrue(result.records[8].isTransfer)
    }

    @Test
    fun noteWithDelimiterAndLineBreakStaysOneRecord() {
        val result = parse(HEADER, row("note" to "\"Кофе; булка\nи чай\""), row("note" to "второй"))
        assertEquals(emptyList(), result.errors)
        assertEquals(listOf("Кофе; булка\nи чай", "второй"), result.records.map { it.note })
        assertEquals(listOf(2, 4), result.records.map { it.line })
    }

    @Test
    fun columnsAreFoundByNameInAnyOrderAndOptionalOnesMayBeMissing() {
        val result = parse(
            "date;transfer;amount;ref_currency_amount;currency;category;account",
            "2026-01-02 03:04:05;FALSE;12.50;4900;EUR;Кафе;Наличные",
        )
        val record = result.records.single()
        assertEquals(Money(1250, CurrencyCode("EUR")), record.amount)
        assertEquals("", record.note)
        assertEquals(null, record.envelopeId)
    }

    @Test
    fun cutOffTrailingColumnsReadAsEmpty() {
        val cut = row("labels" to "", "envelope_id" to "", "custom_category" to "").trimEnd(';')
        val record = parse(HEADER, cut).records.single()
        assertEquals(null, record.label)
    }

    @Test
    fun zeroDigitCurrencyAcceptsWalletsTwoZeroDigits() {
        val result = parse(HEADER, row("currency" to "JPY", "amount" to "-1500.00"))
        assertEquals(Money(-1500, CurrencyCode("JPY")), result.records.single().amount)
    }

    @Test
    fun brokenRowsBecomeErrorsWithLineAndTheRestIsRead() {
        val result = parse(
            HEADER,
            row("amount" to "-1,50"),
            row("date" to "08.11.2026 10:00"),
            row(),
            row("date" to "2026-02-30 10:00:00"),
            row("currency" to "$"),
            row("account" to " "),
            row("transfer" to "yes"),
            row("currency" to "JPY", "amount" to "10.50"),
            row("ref_currency_amount" to ""),
        )
        assertEquals(1, result.records.size)
        assertEquals(
            listOf(
                WalletParseError(WalletProblem.BAD_AMOUNT, 2, "amount", "-1,50"),
                WalletParseError(WalletProblem.BAD_DATE, 3, "date", "08.11.2026 10:00"),
                WalletParseError(WalletProblem.BAD_DATE, 5, "date", "2026-02-30 10:00:00"),
                WalletParseError(WalletProblem.BAD_CURRENCY, 6, "currency", "$"),
                WalletParseError(WalletProblem.EMPTY_ACCOUNT, 7, "account", ""),
                WalletParseError(WalletProblem.BAD_TRANSFER_FLAG, 8, "transfer", "yes"),
                WalletParseError(WalletProblem.AMOUNT_PRECISION, 9, "amount", "10.50"),
                WalletParseError(WalletProblem.BAD_REF_AMOUNT, 10, "ref_currency_amount", ""),
            ),
            result.errors,
        )
    }

    @Test
    fun notAWalletFileNamesMissingColumns() {
        val result = parse("Date,Amount,Note", "2026-01-01,5,x")
        assertEquals(emptyList(), result.records)
        assertEquals(
            listOf("account", "category", "currency", "amount", "ref_currency_amount", "date", "transfer"),
            result.errors.map { it.column },
        )
        assertTrue(result.errors.all { it.problem == WalletProblem.MISSING_COLUMN })
    }

    @Test
    fun emptyFileAndHeaderOnly() {
        assertEquals(listOf(WalletParseError(WalletProblem.EMPTY_FILE)), parse("").errors)
        assertEquals(listOf(WalletParseError(WalletProblem.EMPTY_FILE)), parse(HEADER).errors)
    }

    @Test
    fun unterminatedQuoteStopsTheWholeFile() {
        val result = parse(HEADER, row(), row("note" to "\"не закрыта"), row())
        assertEquals(emptyList(), result.records)
        assertEquals(listOf(WalletParseError(WalletProblem.UNTERMINATED_QUOTE, line = 3)), result.errors)
    }
}
