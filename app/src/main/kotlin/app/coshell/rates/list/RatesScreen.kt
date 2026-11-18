package app.coshell.rates.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import androidx.compose.foundation.layout.PaddingValues
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.rates.list.components.ConverterCard
import app.coshell.rates.list.components.ConverterKeypadSheet
import app.coshell.rates.list.components.CurrentRateRow
import app.coshell.rates.list.components.ManualRateSheet
import app.coshell.rates.list.components.ManualRateRow
import app.coshell.rates.list.components.RatesSectionHeader
import app.coshell.rates.list.components.RatesSources
import app.coshell.rates.list.components.RefreshBlock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.itemsIndexed
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellAddFab
import app.coshell.core.designsystem.components.RowGroupPosition
import app.coshell.core.designsystem.components.rowGroupSegment
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.ConfirmDialog
import app.coshell.ui.top.ScreenTopBar

/** "More → Rates": the converter (#319), today's rates with the converted amount, "Update now", manual rates, sources. */
@Composable
fun RatesScreen(state: RatesUiState, actions: RatesActions, editorActions: ManualRateActions = ManualRateActions()) {
    var keypadOpen by rememberSaveable { mutableStateOf(false) }
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.rates_title), onBack = actions.onBack)
        },
        floatingActionButton = { CoShellAddFab(stringResource(R.string.rates_manual_add), actions.onAddManual) },
    ) { padding ->
        // The converter appears after the rates load, *above* the already shown refresh block; a lazy list keeps its
        // position by key, so it would stay scrolled one item down and hide the converter under the top capsule.
        val listState = rememberLazyListState()
        val hasConverter = state.current.isNotEmpty()
        LaunchedEffect(hasConverter) {
            if (hasConverter && listState.firstVisibleItemIndex == 1 && listState.firstVisibleItemScrollOffset == 0) {
                listState.scrollToItem(0)
            }
        }
        LazyColumn(Modifier.fillMaxSize(), state = listState, contentPadding = underBars(padding, PaddingValues(bottom = Sizes.fabClearance))) {
            // «Сколько это» right on top (#319, А): the rows below follow the typed amount.
            if (state.current.isNotEmpty()) {
                item(key = "converter") {
                    ConverterCard(state.converter, onAmountClick = { keypadOpen = true }, onCurrencyClick = { pickerOpen = true })
                }
            }
            // Status first (mockup 11): loaded or not, and the refresh button under the thumb's reach.
            item(key = "refresh") { RefreshBlock(state.lastDownload, state.refresh, actions.onRefresh) }
            item(key = "current-header") {
                RatesSectionHeader(
                    stringResource(R.string.rates_accounts),
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
                RatesSectionHeader(stringResource(R.string.rates_manual))
            }
            items(state.manual, key = { "manual-${it.rate.date}-${it.rate.from}-${it.rate.to}" }) { item ->
                ManualRateRow(item, onClick = { actions.onManualClick(item) }, onDelete = { actions.onManualDelete(item) })
            }
            item(key = "sources") { RatesSources() }
        }
    }

    if (keypadOpen) ConverterKeypadSheet(state.converter, actions.onConverterKey, onClosed = { keypadOpen = false })
    if (pickerOpen) {
        ChoiceSheet(
            title = stringResource(R.string.rates_converter_pick),
            options = state.converter.options.map { ChoiceOption(it, it) },
            selected = state.converter.from,
            onPick = { code ->
                pickerOpen = false
                actions.onConverterFrom(code)
            },
            onDismiss = { pickerOpen = false },
        )
    }
    state.editor?.let { ManualRateSheet(it, editorActions) }
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
