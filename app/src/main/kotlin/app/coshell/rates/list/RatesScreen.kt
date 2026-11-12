package app.coshell.rates.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.rates.list.components.CurrentRateRow
import app.coshell.rates.list.components.ManualRateDialog
import app.coshell.rates.list.components.ManualRateRow
import app.coshell.rates.list.components.RatesSectionHeader
import app.coshell.rates.list.components.RatesSources
import app.coshell.rates.list.components.RefreshBlock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.itemsIndexed
import app.coshell.core.designsystem.components.RowGroupPosition
import app.coshell.core.designsystem.components.rowGroupSegment
import app.coshell.ui.form.ConfirmDialog
import app.coshell.ui.list.ActionRow
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** "More → Rates": today's rates, "Update now", manual rates, sources. */
@Composable
fun RatesScreen(state: RatesUiState, actions: RatesActions, editorActions: ManualRateActions = ManualRateActions()) {
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.rates_title), onBack = actions.onBack)
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            // Status first (mockup 11): loaded or not, and the refresh button under the thumb's reach.
            item(key = "refresh") { RefreshBlock(state.lastDownload, state.refresh, actions.onRefresh) }
            item(key = "current-header") {
                RatesSectionHeader(
                    stringResource(R.string.rates_current),
                    stringResource(R.string.rates_current_hint).takeIf { state.current.isNotEmpty() },
                )
            }
            if (state.current.isEmpty() && !state.isLoading) {
                item(key = "current-empty") { MutedLine(stringResource(R.string.rates_no_currencies)) }
            }
            // К5: today's rates in one card; each lazy item draws its own piece of it.
            itemsIndexed(state.current, key = { _, it -> "current-${it.currency}" }) { index, item ->
                val position = RowGroupPosition(index, state.current.size)
                Box(Modifier.padding(horizontal = Spacing.l).rowGroupSegment(position)) {
                    CurrentRateRow(item, state.base) { actions.onCurrentClick(item) }
                }
            }

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
