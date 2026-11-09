package app.coshell.core.domain.importer.wallet

import app.coshell.core.domain.importer.csv.CsvReader
import app.coshell.core.domain.importer.csv.CsvResult
import app.coshell.core.domain.importer.csv.CsvRow
import app.coshell.core.model.CurrencyCode
import kotlinx.datetime.LocalDateTime

/**
 * Turns the text of a Wallet CSV export into [WalletRecord]s (format — ARCHITECTURE §8).
 *
 * Columns are found by header name, so their order and extra columns do not matter. A broken row
 * becomes a [WalletParseError] with its line number and the rest of the file is still read; the
 * wizard shows the errors and lets the user import what was read or stop.
 * `type` is localized in Wallet and is ignored — the sign of `amount` tells expense from income.
 */
object WalletCsvParser {

    const val DELIMITER = ';'

    private const val ACCOUNT = "account"
    private const val CATEGORY = "category"
    private const val CURRENCY = "currency"
    private const val AMOUNT = "amount"
    private const val REF_AMOUNT = "ref_currency_amount"
    private const val NOTE = "note"
    private const val DATE = "date"
    private const val TRANSFER = "transfer"
    private const val PAYEE = "payee"
    private const val LABELS = "labels"
    private const val ENVELOPE = "envelope_id"

    /** Without these the file is not a Wallet export. The rest are optional. */
    private val REQUIRED = listOf(ACCOUNT, CATEGORY, CURRENCY, AMOUNT, REF_AMOUNT, DATE, TRANSFER)

    private val DATE_PATTERN = Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""")

    fun parse(text: String): WalletParseResult {
        val rows = when (val csv = CsvReader.read(text, DELIMITER)) {
            is CsvResult.UnterminatedQuote -> return fileError(
                WalletParseError(WalletProblem.UNTERMINATED_QUOTE, line = csv.line),
            )
            is CsvResult.Success -> csv.rows
        }
        val header = rows.firstOrNull() ?: return fileError(WalletParseError(WalletProblem.EMPTY_FILE))
        val columns = header.fields.withIndex().associate { (i, name) -> name.trim().lowercase() to i }
        val missing = REQUIRED.filter { it !in columns }
        if (missing.isNotEmpty()) {
            return fileError(
                *missing.map { WalletParseError(WalletProblem.MISSING_COLUMN, column = it) }.toTypedArray(),
            )
        }
        if (rows.size == 1) return fileError(WalletParseError(WalletProblem.EMPTY_FILE))

        val records = mutableListOf<WalletRecord>()
        val errors = mutableListOf<WalletParseError>()
        for (row in rows.drop(1)) {
            when (val parsed = parseRow(row, columns)) {
                is RowResult.Ok -> records += parsed.record
                is RowResult.Failed -> errors += parsed.error
            }
        }
        return WalletParseResult(records, errors)
    }

    private sealed interface RowResult {
        data class Ok(val record: WalletRecord) : RowResult
        data class Failed(val error: WalletParseError) : RowResult
    }

    private fun parseRow(row: CsvRow, columns: Map<String, Int>): RowResult {
        // Trailing empty columns may be cut off by other tools: a missing cell reads as empty.
        fun cell(name: String): String = columns[name]?.let { row.fields.getOrNull(it) }?.trim().orEmpty()
        fun fail(problem: WalletProblem, column: String) =
            RowResult.Failed(WalletParseError(problem, row.line, column, cell(column)))

        val account = cell(ACCOUNT).ifEmpty { return fail(WalletProblem.EMPTY_ACCOUNT, ACCOUNT) }
        val currency = CurrencyCode.parseOrNull(cell(CURRENCY))
            ?: return fail(WalletProblem.BAD_CURRENCY, CURRENCY)
        val amountDecimal = WalletDecimal.parse(cell(AMOUNT)) ?: return fail(WalletProblem.BAD_AMOUNT, AMOUNT)
        val amount = amountDecimal.toMoney(currency) ?: return fail(WalletProblem.AMOUNT_PRECISION, AMOUNT)
        val refAmount = WalletDecimal.parse(cell(REF_AMOUNT))
            ?: return fail(WalletProblem.BAD_REF_AMOUNT, REF_AMOUNT)
        val dateTime = parseDate(cell(DATE)) ?: return fail(WalletProblem.BAD_DATE, DATE)
        val isTransfer = when (cell(TRANSFER).lowercase()) {
            "true" -> true
            "false" -> false
            else -> return fail(WalletProblem.BAD_TRANSFER_FLAG, TRANSFER)
        }
        return RowResult.Ok(
            WalletRecord(
                line = row.line,
                account = account,
                category = cell(CATEGORY),
                amount = amount,
                refAmount = refAmount,
                note = cell(NOTE),
                dateTime = dateTime,
                isTransfer = isTransfer,
                payee = cell(PAYEE).ifEmpty { null },
                label = cell(LABELS).ifEmpty { null },
                envelopeId = cell(ENVELOPE).ifEmpty { null },
            ),
        )
    }

    /** "2026-11-08 11:23:53" → local date-time; anything else (or an impossible date) → `null`. */
    private fun parseDate(raw: String): LocalDateTime? {
        if (!DATE_PATTERN.matches(raw)) return null
        return runCatching { LocalDateTime.parse(raw.replace(' ', 'T')) }.getOrNull()
    }

    private fun fileError(vararg errors: WalletParseError) = WalletParseResult(emptyList(), errors.toList())
}
