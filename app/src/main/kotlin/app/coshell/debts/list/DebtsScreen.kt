package app.coshell.debts.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAddFab
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellEmptyState
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.hints.HintId
import app.coshell.hints.WordHint
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** What the screen can ask for; defaults keep previews short. */
data class DebtsActions(
    val onBack: () -> Unit = {},
    val onTab: (Int) -> Unit = {},
    val onAdd: () -> Unit = {},
    val onOpen: (String) -> Unit = {},
    val onToggleClosed: () -> Unit = {},
)

/**
 * «Долги» (#552, boards `DebtListA`, `DebtMany`, `DebtEmpty`): two tabs — «Мне должны» and «Я должен» — the total per currency
 * on top, the lines below, closed debts folded at the bottom. The «+» opens the new-debt sheet.
 */
@Composable
fun DebtsScreen(state: DebtsUiState, actions: DebtsActions) {
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.debts_title), onBack = actions.onBack) },
        floatingActionButton = { CoShellAddFab(stringResource(R.string.debts_add), actions.onAdd) },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        if (state.nothingAtAll) {
            Column(Modifier.padding(underBars(padding))) {
                WordHint(HintId.DEBTS, R.string.hint_debts, Modifier.padding(horizontal = Spacing.l), emptyScreen = true)
                CoShellEmptyState(iconRes = DesignR.drawable.ic_person_add, text = stringResource(R.string.debts_empty))
            }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = underBars(padding, PaddingValues(top = Spacing.s, bottom = Spacing.l)), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            item(key = "word-hint", contentType = "hint") { WordHint(HintId.DEBTS, R.string.hint_debts, Modifier.padding(horizontal = Spacing.l)) }
            item(key = "tabs", contentType = "tabs") {
                CoShellSegmented(
                    labels = listOf(stringResource(R.string.debts_tab_lent), stringResource(R.string.debts_tab_borrowed)),
                    selected = state.tab,
                    onSelect = actions.onTab,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l),
                )
            }
            item(key = "total", contentType = "hint") {
                val lent = state.tab == 0
                val text = when {
                    state.totals.isNotEmpty() -> stringResource(if (lent) R.string.debts_total_lent else R.string.debts_total_borrowed, state.totals.joinToString(" · "))
                    else -> stringResource(if (lent) R.string.debts_tab_empty_lent else R.string.debts_tab_empty_borrowed)
                }
                CoShellHint(text, Modifier.padding(horizontal = Spacing.xl))
            }
            if (state.active.isNotEmpty()) {
                item(key = "active", contentType = "card") { DebtsCard(state.active, actions.onOpen) }
            }
            if (state.closed.isNotEmpty()) {
                item(key = "closedToggle", contentType = "link") {
                    Column(Modifier.padding(horizontal = Spacing.xl)) {
                        CoShellSectionLink(
                            if (state.closedShown) stringResource(R.string.debts_closed_hide) else stringResource(R.string.debts_closed_show, state.closed.size),
                            actions.onToggleClosed,
                        )
                    }
                }
                if (state.closedShown) item(key = "closed", contentType = "card") { DebtsCard(state.closed, actions.onOpen) }
            }
        }
    }
}

@Composable
private fun DebtsCard(rows: List<DebtRowUi>, onOpen: (String) -> Unit) {
    CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l)) {
        rows.forEachIndexed { index, row ->
            androidx.compose.runtime.key(row.id) {
                if (index > 0) CoShellDivider()
                DebtRow(row, onClick = { onOpen(row.id) })
            }
        }
    }
}

/** One line: the person, what is left (of how much), the due day in words; the meter shows how much came back. */
@Composable
internal fun DebtRow(row: DebtRowUi, onClick: () -> Unit) {
    val due = when (row.due) {
        DebtDue.NONE -> stringResource(R.string.debts_no_due)
        DebtDue.SET -> stringResource(R.string.debts_due_date, row.dueDate)
        DebtDue.TODAY -> stringResource(R.string.debts_due_today)
        DebtDue.SOON -> pluralStringResource(R.plurals.debts_due_in, row.days, row.days)
        DebtDue.LATE -> pluralStringResource(R.plurals.debts_late, row.days, row.days)
    }
    val sub = buildList {
        if (row.ofTotal.isNotEmpty()) add(stringResource(R.string.debts_left_of, row.left, row.ofTotal))
        add(if (row.closed) stringResource(R.string.debts_closed_label) else due)
        if (row.note.isNotEmpty()) add(row.note)
    }.joinToString(" · ")
    CoShellListRow(
        title = row.person,
        subtitle = sub,
        subtitleMaxLines = 2,
        trailing = { Text(row.left) },
        onClick = onClick,
    )
}
