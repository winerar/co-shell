package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.Money
import kotlinx.datetime.LocalDateTime

/**
 * One row of a Wallet (BudgetBakers) CSV export, already validated. Format — ARCHITECTURE §8.
 *
 * @property line 1-based line of the row in the file (for error messages and previews).
 * @property amount signed amount in the account currency: `< 0` — money left the account.
 * @property refAmount the same amount in Wallet's base currency (not named in the file).
 * @property dateTime local time without a zone; the import wizard picks the zone.
 * @property label Wallet allows at most one label per record.
 * @property envelopeId Wallet's language-independent category id — only a matching hint.
 */
data class WalletRecord(
    val line: Int,
    val account: String,
    val category: String,
    val amount: Money,
    val refAmount: WalletDecimal,
    val note: String,
    val dateTime: LocalDateTime,
    val isTransfer: Boolean,
    val payee: String?,
    val label: String?,
    val envelopeId: String?,
)

/** Why a row (or the whole file) could not be read. The app turns it into Russian text. */
enum class WalletProblem {
    /** The file has no header row or no rows at all. */
    EMPTY_FILE,

    /** A quoted field is never closed (from that line on the file is unreadable). */
    UNTERMINATED_QUOTE,

    /** The header lacks a required column — probably not a Wallet export. */
    MISSING_COLUMN,
    EMPTY_ACCOUNT,
    BAD_CURRENCY,
    BAD_AMOUNT,

    /** More fraction digits than the currency has, and they are not zeros. */
    AMOUNT_PRECISION,
    BAD_REF_AMOUNT,
    BAD_DATE,
    BAD_TRANSFER_FLAG,
}

/**
 * @property line 1-based file line, `null` for problems of the whole file.
 * @property column header name of the offending column, if any.
 * @property value the raw text that failed, if any.
 */
data class WalletParseError(
    val problem: WalletProblem,
    val line: Int? = null,
    val column: String? = null,
    val value: String? = null,
)

/** Records that were read and errors of rows that were not; a file-level error leaves no records. */
data class WalletParseResult(
    val records: List<WalletRecord>,
    val errors: List<WalletParseError>,
)
