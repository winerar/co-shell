package app.coshell.importer.wallet

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.domain.importer.wallet.WalletImportProblem
import app.coshell.core.domain.importer.wallet.WalletParseError
import app.coshell.core.domain.importer.wallet.WalletProblem

// Russian text for the wizard's domain enums.

@Composable
fun WalletImportStep.title(): String = stringResource(
    when (this) {
        WalletImportStep.START -> R.string.import_step_start
        WalletImportStep.SUMMARY -> R.string.import_step_summary
        WalletImportStep.ACCOUNTS -> R.string.import_step_accounts
        WalletImportStep.CATEGORIES -> R.string.import_step_categories
        WalletImportStep.TRANSFERS -> R.string.import_step_transfers
        WalletImportStep.PREVIEW -> R.string.import_step_preview
        WalletImportStep.DONE -> R.string.import_step_done
    },
)

@Composable
fun WalletImportError.text(): String = stringResource(
    when (this) {
        WalletImportError.READ_FAILED -> R.string.import_error_read
        WalletImportError.NOT_WALLET -> R.string.import_error_not_wallet
        WalletImportError.BACKUP_FAILED -> R.string.import_error_backup
        WalletImportError.SAVE_FAILED -> R.string.import_error_save
    },
)

/** «Строка 12: неверное значение «abc» в колонке amount». */
@Composable
fun WalletParseError.text(): String {
    val what = when (problem) {
        WalletProblem.EMPTY_FILE -> stringResource(R.string.import_problem_empty_file)
        WalletProblem.UNTERMINATED_QUOTE -> stringResource(R.string.import_problem_quote)
        WalletProblem.MISSING_COLUMN -> stringResource(R.string.import_problem_column, column.orEmpty())
        else -> stringResource(R.string.import_problem_value, value.orEmpty(), column.orEmpty())
    }
    return line?.let { stringResource(R.string.import_parse_error_line, it, what) } ?: what
}

@Composable
fun WalletImportProblem.text(): String = stringResource(
    when (this) {
        WalletImportProblem.UNMAPPED -> R.string.import_issue_unmapped
        WalletImportProblem.NEEDS_RATE -> R.string.import_issue_needs_rate
        WalletImportProblem.ZERO_AMOUNT -> R.string.import_issue_zero
    },
)
