package app.coshell.currencies.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.currencies.list.components.AddCurrencyDialog
import app.coshell.currencies.list.components.ReportCurrencyRow
import app.coshell.ui.list.ActionRow
import app.coshell.core.designsystem.R as DesignR

/** "More → Report currencies": main currency first, tap another to make it main, add / remove. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportCurrenciesScreen(state: ReportCurrenciesUiState, actions: ReportCurrenciesActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_currencies_title)) },
                navigationIcon = {
                    CoShellIconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item(key = "hint") {
                Text(
                    text = stringResource(R.string.report_currencies_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
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
