package app.coshell.importer.wallet.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.importer.wallet.WalletParseError
import app.coshell.core.domain.importer.wallet.WalletSummary
import app.coshell.importer.wallet.text
import app.coshell.ui.format.formatShortDate
import app.coshell.ui.list.SectionTitle

/** Step 2: what the file holds before anything is mapped, and rows that could not be read. */
fun LazyListScope.summaryStep(summary: WalletSummary, errors: List<WalletParseError>) {
    item(key = "records") {
        val period = if (summary.firstDate != null && summary.lastDate != null) {
            stringResource(R.string.import_summary_period, formatShortDate(summary.firstDate!!), formatShortDate(summary.lastDate!!))
        } else {
            null
        }
        CoShellListRow(title = stringResource(R.string.import_summary_records, summary.records), subtitle = period)
    }
    item(key = "accounts") {
        CoShellListRow(
            title = stringResource(R.string.import_summary_accounts, summary.accounts.size),
            subtitle = stringResource(R.string.import_summary_currencies, summary.currencies.map { it.value }.sorted().joinToString(", ")),
        )
    }
    item(key = "categories") { CoShellListRow(title = stringResource(R.string.import_summary_categories, summary.categories.size)) }
    item(key = "transfers") {
        CoShellListRow(title = stringResource(R.string.import_summary_transfers, summary.pairedTransfers, summary.unpairedTransfers))
    }
    if (summary.labels.isNotEmpty()) {
        item(key = "labels") {
            CoShellListRow(title = stringResource(R.string.import_summary_labels, summary.labels.sorted().joinToString(", ")), subtitleMaxLines = 3)
        }
    }
    if (errors.isNotEmpty()) {
        item(key = "errors") { SectionTitle(stringResource(R.string.import_parse_errors_title, errors.size)) }
        items(errors.take(MAX_ERRORS), key = { "err-${it.line}-${it.column}" }) { error ->
            Text(
                error.text(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.xs),
            )
        }
    }
}

/** Enough to see what is wrong; a broken file can have thousands. */
private const val MAX_ERRORS = 20
