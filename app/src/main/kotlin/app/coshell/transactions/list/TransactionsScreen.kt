package app.coshell.transactions.list

import app.coshell.hints.HintSlot
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.transactions.duplicates.DuplicateSheet
import app.coshell.transactions.offer.RecurringOffer
import app.coshell.transactions.offer.RecurringOfferCard
import app.coshell.core.domain.hints.HintId
import app.coshell.hints.CoachHint
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.motion.listItemMotion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.components.DayHeader
import app.coshell.transactions.list.components.FilterBar
import app.coshell.transactions.list.components.SelectionActions
import app.coshell.transactions.list.components.TransactionRow
import app.coshell.transactions.list.components.dayLabel
import app.coshell.ui.bar.ReplaceBottomBar
import app.coshell.ui.list.LoadMoreEffect
import app.coshell.ui.top.TabOrSelectionTopBar
import app.coshell.hints.WordHint
import app.coshell.ui.top.TabSearch
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import kotlinx.datetime.LocalDate

/**
 * «Операции»: a loupe in the header (#394 Б) and filter chips on top, days with their net on the right. Rows have no swipes — a
 * horizontal swipe flips tabs (issue #112); a long press chooses rows (mockup 19В): the header counts them,
 * the bottom bar turns into «⧉ · Изменить · Удалить · N», back or «✕» leaves the mode.
 * Older pages load while scrolling ([LoadMoreEffect]); once scrolled, the header's second line names the day at
 * the top with its net (header poll А, 2026-11-15) — so day titles no longer stick. Holding an amount shows
 * it in the report currencies at the operation's day ([peek], #318).
 */
@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    selection: Selection = Selection(),
    actions: TransactionsActions = TransactionsActions(),
    peek: AmountPeek? = null,
    /** «Сделать регулярным?» (#789); shown above the list without a filter. */
    offer: RecurringOffer? = null,
) {
    val noRate = stringResource(R.string.amount_peek_no_rate)
    // Gesture tips: editing by a tap after the first entry, selecting by a hold once there are three; holding a sum (other currencies)
    // from five; the app icon's shortcuts from ten (#513, audit 2026-11-25).
    val rowCount = state.days.sumOf { it.rows.size }
    val tips = operationTips(rowCount, state.repeatedSpending, state.filter.isActive)
    CoachHint(tips, enabled = !state.isLoading && !selection.active && tips.isNotEmpty())
    val scroll = rememberTabScrollBehavior()
    val listState = rememberLazyListState()
    LoadMoreEffect(listState, enabled = state.hasMore, onLoadMore = actions.onLoadMore)
    BackHandler(enabled = selection.active, onBack = actions.onClearSelection)
    ReplaceBottomBar(active = selection.active) {
        SelectionActions(
            count = selection.count,
            onDuplicate = { selection.single?.let(actions.onDuplicate) },
            onEdit = actions.onEditSelected,
            onDelete = actions.onDeleteSelected,
        )
    }
    val topDay by remember(state.days, state.filter.isActive) {
        derivedStateOf { dayAt(state, listState.firstVisibleItemIndex) }
    }
    val subtitle = topDay?.let { day -> listOfNotNull(dayLabel(day), day.total).joinToString(SUBTITLE_SEPARATOR) }
    // The pair asked about (#791) and the row that was tapped; the sheet lives only while the screen does.
    var duplicate by remember { mutableStateOf<Pair<TransactionsUiState.RowDuplicate, String>?>(null) }
    duplicate?.let { (pair, tapped) ->
        DuplicateSheet(pair, tapped, actions.onDeleteDuplicate, actions.onDifferent, actions.onTransactionClick, onClosed = { duplicate = null })
    }
    Scaffold(
        topBar = {
            TabOrSelectionTopBar(
                title = stringResource(R.string.nav_transactions),
                scroll = scroll,
                selectedCount = selection.count,
                onClearSelection = actions.onClearSelection,
                subtitle = subtitle,
                search = TabSearch(
                    query = state.filter.query,
                    onQueryChange = { q -> actions.onFilter { it.copy(query = q) } },
                    placeholder = stringResource(R.string.transactions_search),
                    openLabel = stringResource(R.string.transactions_search_open),
                    clearLabel = stringResource(R.string.search_clear),
                    closeLabel = stringResource(R.string.search_close),
                ),
            )
        },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        if (state.isLoading) return@Scaffold
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = underBars(padding, PaddingValues(bottom = Sizes.fabClearance)),
        ) {
            if (state.days.isNotEmpty() || state.filter.isActive) {
                item(key = "filters") {
                    // The tags word (#513) sits in the filters item, so [dayAt] keeps counting one item for the top.
                    Column {
                        FilterBar(state, actions.onFilter, Modifier.padding(bottom = Spacing.s))
                        HintSlot(Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.s))
                        if (offer != null && !state.filter.isActive && !selection.active) {
                            RecurringOfferCard(offer, actions.onOfferAccept, actions.onOfferLater, Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.s))
                        }
                        if (rowCount >= TAGS_HINT_ROWS && !state.filter.isActive) {
                            WordHint(HintId.TAGS, R.string.hint_tags, Modifier.padding(horizontal = Spacing.l))
                        }
                        if (rowCount >= NOTES_HINT_ROWS && !state.filter.isActive) {
                            WordHint(HintId.NOTES, R.string.hint_notes, Modifier.padding(horizontal = Spacing.l))
                        }
                    }
                }
            }
            if (state.days.isEmpty()) {
                item(key = "empty") { EmptyState(filtered = state.filter.isActive, onReset = { actions.onFilter { ListFilter() } }) }
                return@LazyColumn
            }
            state.days.forEach { day ->
                item(key = day.date.toString()) { DayHeader(day) }
                items(day.rows, key = { it.id }) { row ->
                    // Rows slide in, out and into place (design-questions §6).
                    Box(listItemMotion()) {
                        TransactionRow(
                            row = row,
                            // While choosing, a tap adds or removes the row instead of opening it.
                            onClick = when {
                                selection.active -> actions.onToggle
                                row.duplicate != null -> { id -> duplicate = row.duplicate to id }
                                else -> actions.onTransactionClick
                            },
                            onLongClick = actions.onLongPress,
                            selected = row.id in selection,
                            peek = peek?.takeIf { it.rowId == row.id },
                            onPeek = if (selection.active) null else ({ actions.onPeek(row, day.date, noRate) }),
                            onPeekEnd = actions.onPeekEnd,
                        )
                    }
                }
            }
        }
    }
}

/** «Теги» word hint appears in «Операции» from this many rows (`hints.md`). */
internal const val TAGS_HINT_ROWS = 10

/** Many small expenses: the word about «Быстрые заметки» (#586). */
internal const val NOTES_HINT_ROWS = 20

/** «Удерживайте сумму…» (#513): from five rows, when there is something to look at in other currencies. */
internal const val HOLD_AMOUNT_ROWS = 5

/** The gesture tips of the tab for [rowCount] entries, in the order they are offered (#513, #579). */
internal fun operationTips(rowCount: Int, repeatedSpending: Boolean, filterActive: Boolean): Map<HintId, Int> = buildMap {
    if (rowCount >= 1) put(HintId.EDIT_TAP, R.string.hint_edit_tap)
    if (rowCount >= 3) put(HintId.HOLD_SELECT, R.string.hint_hold_select)
    if (repeatedSpending && !filterActive) put(HintId.TEMPLATE, R.string.hint_template)
    if (rowCount >= SHARE_RECEIPT_ROWS && !filterActive) put(HintId.SHARE_RECEIPT, R.string.hint_share_receipt)
    if (rowCount >= HOLD_AMOUNT_ROWS) put(HintId.HOLD_AMOUNT, R.string.hint_hold_amount)
    if (rowCount >= SHORTCUTS_ROWS && !filterActive) put(HintId.APP_SHORTCUTS, R.string.hint_app_shortcuts)
}

/** A receipt can be shared into the app from a bank or a shop: told after the third entry (#579). */
internal const val SHARE_RECEIPT_ROWS = 3

/** «Удерживайте значок приложения…» (#555 shortcuts): from ten rows — someone who records often. */
internal const val SHORTCUTS_ROWS = 10

/**
 * The day whose title or rows are at the top of the list at item [index]: the list is the filter bar (when
 * shown), then each day's title and its rows — the same order as the LazyColumn above.
 */
internal fun dayAt(state: TransactionsUiState, index: Int): TransactionsUiState.Day? {
    var at = if (state.days.isNotEmpty() || state.filter.isActive) 1 else 0
    var found: TransactionsUiState.Day? = null
    for (day in state.days) {
        if (at > index) break
        found = day
        at += 1 + day.rows.size
    }
    return found
}

private const val SUBTITLE_SEPARATOR = " · "

/** Everything the list can ask for; defaults make previews short. */
data class TransactionsActions(
    val onTransactionClick: (String) -> Unit = {},
    val onLoadMore: () -> Unit = {},
    val onFilter: ((ListFilter) -> ListFilter) -> Unit = {},
    val onLongPress: (String) -> Unit = {},
    val onToggle: (String) -> Unit = {},
    val onClearSelection: () -> Unit = {},
    val onDuplicate: (String) -> Unit = {},
    val onEditSelected: () -> Unit = {},
    val onDeleteSelected: () -> Unit = {},
    /** Hold the amount of a row of a day (#318); the text is «нет курса». */
    val onPeek: (TransactionsUiState.Row, LocalDate, String) -> Unit = { _, _, _ -> },
    val onPeekEnd: () -> Unit = {},
    /** «Похоже на дубль» (#791): «Удалить вторую» (the newer id) and «Это разные» (the pair key). */
    val onDeleteDuplicate: (String) -> Unit = {},
    val onDifferent: (String) -> Unit = {},
    /** «Сделать регулярным» and «Не сейчас» of the regular-payment card (#789). */
    val onOfferAccept: () -> Unit = {},
    val onOfferLater: () -> Unit = {},
)

@Composable
private fun EmptyState(filtered: Boolean, onReset: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        Text(
            text = stringResource(if (filtered) R.string.transactions_nothing else R.string.transactions_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (filtered) CoShellTextButton(onClick = onReset) { Text(stringResource(R.string.filter_reset)) }
    }
}
