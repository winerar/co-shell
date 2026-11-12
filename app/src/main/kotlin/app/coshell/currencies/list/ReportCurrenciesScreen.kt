package app.coshell.currencies.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.currencies.list.components.AddCurrencyDialog
import app.coshell.currencies.list.components.BalancePreviewCard
import app.coshell.currencies.list.components.ReportCurrencyRow
import app.coshell.ui.list.ActionRow
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** "More → Report currencies": main currency first, tap another to make it main, add / remove. */
@Composable
fun ReportCurrenciesScreen(state: ReportCurrenciesUiState, actions: ReportCurrenciesActions) {
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.report_currencies_title), onBack = actions.onBack)
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item(key = "hint") {
                Text(
                    text = stringResource(R.string.report_currencies_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s),
                )
            }
            item(key = "preview") { BalancePreviewCard(state.preview) }
            itemsIndexed(state.currencies, key = { _, code -> code }) { index, code ->
                ReportCurrencyRow(
                    code = code,
                    isMain = index == 0,
                    canRemove = state.canRemove,
                    onMakeMain = { actions.onMakeMain(code) },
                    onRemove = { actions.onRemove(code) },
                )
            }
            item(key = "add") {
                ActionRow(DesignR.drawable.ic_add, stringResource(R.string.report_currencies_add), actions.onAdd)
            }
        }
    }
    state.adding?.let { AddCurrencyDialog(it, actions) }
}
