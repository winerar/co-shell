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
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.importer.wallet.WalletImportPlan
import app.coshell.core.model.MoneyFormat
import app.coshell.importer.wallet.text
import app.coshell.ui.list.SectionTitle

/** Step 6: N new, M already loaded, K needing a decision, and every touched account's balance after. */
fun LazyListScope.previewStep(plan: WalletImportPlan, moneyFormat: MoneyFormat) {
    if (plan.newCount == 0) {
        item(key = "nothing") { CoShellMessage(stringResource(R.string.import_preview_nothing), isError = false, Modifier.padding(horizontal = Spacing.l)) }
    }
    item(key = "new") {
        CoShellListRow(
            title = stringResource(R.string.import_preview_new, plan.newCount),
            subtitle = stringResource(R.string.import_preview_new_accounts, plan.newAccounts, plan.write.categories.size),
        )
    }
    if (plan.alreadyImported > 0) item(key = "known") { CoShellListRow(title = stringResource(R.string.import_preview_known, plan.alreadyImported)) }
    if (plan.skipped > 0) item(key = "skipped") { CoShellListRow(title = stringResource(R.string.import_preview_skipped, plan.skipped)) }
    if (plan.balancesAfter.isNotEmpty()) {
        item(key = "balances") { SectionTitle(stringResource(R.string.import_preview_balances)) }
        items(plan.balancesAfter.entries.sortedBy { plan.accountNames[it.key].orEmpty() }, key = { "bal-${it.key}" }) { (id, balance) ->
            CoShellListRow(title = plan.accountNames[id].orEmpty(), trailing = { Text(moneyFormat.format(balance)) })
        }
    }
    if (plan.issues.isNotEmpty()) {
        item(key = "issues") { SectionTitle(stringResource(R.string.import_preview_issues, plan.issues.size)) }
        items(plan.issues.take(MAX_ISSUES), key = { "iss-${it.line}" }) { issue ->
            Text(
                stringResource(R.string.import_issue_line, issue.line, issue.problem.text()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.xs),
            )
        }
    }
}

private const val MAX_ISSUES = 30
