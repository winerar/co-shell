package app.coshell.rates.list

import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.rates.list.components.CurrentRateRow
import app.coshell.rates.list.components.ManualRateDialog
import app.coshell.rates.list.components.ManualRateRow
import app.coshell.rates.list.components.RatesSectionHeader
import app.coshell.rates.list.components.RatesSources
import app.coshell.rates.list.components.RefreshBlock
import app.coshell.ui.form.ConfirmDialog
import app.coshell.ui.list.ActionRow
import app.coshell.core.designsystem.R as DesignR

/** "More → Rates": today's rates, "Update now", manual rates, sources. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesScreen(state: RatesUiState, actions: RatesActions, editorActions: ManualRateActions = ManualRateActions()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rates_title)) },
                navigationIcon = {
                    CoShellIconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item(key = "current-header") {
                RatesSectionHeader(
                    stringResource(R.string.rates_current),
                    stringResource(R.string.rates_current_hint).takeIf { state.current.isNotEmpty() },
                )
            }
            if (state.current.isEmpty() && !state.isLoading) {
                item(key = "current-empty") { MutedLine(stringResource(R.string.rates_no_currencies)) }
            }
            items(state.current, key = { "current-${it.currency}" }) { item ->
                CurrentRateRow(item, state.base) { actions.onCurrentClick(item) }
            }
            item(key = "refresh") { RefreshBlock(state.lastDownload, state.refresh, actions.onRefresh) }

            item(key = "manual-header") {
                RatesSectionHeader(stringResource(R.string.rates_manual), stringResource(R.string.rates_manual_hint))
            }
            items(state.manual, key = { "manual-${it.rate.date}-${it.rate.from}-${it.rate.to}" }) { item ->
                ManualRateRow(item, onClick = { actions.onManualClick(item) }, onDelete = { actions.onManualDelete(item) })
            }
            item(key = "manual-add") {
                ActionRow(DesignR.drawable.ic_add, stringResource(R.string.rates_manual_add), actions.onAddManual)
            }
            item(key = "sources") { RatesSources() }
        }
    }

    state.editor?.let { ManualRateDialog(it, editorActions) }
    if (state.pendingDelete != null) {
        ConfirmDialog(
            title = R.string.rates_manual_delete_title,
            text = R.string.rates_manual_delete_text,
            confirmLabel = R.string.action_delete,
            onConfirm = actions.onConfirmDelete,
            onDismiss = actions.onCancelDelete,
        )
    }
}

@Composable
private fun MutedLine(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s),
    )
}
