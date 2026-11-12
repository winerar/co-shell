package app.coshell.budgets.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.list.components.BudgetFilterChips
import app.coshell.budgets.list.components.BudgetSummaryCard
import app.coshell.budgets.list.components.EnvelopeRow
import app.coshell.budgets.list.components.MonthSwitch
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar

/**
 * «Бюджеты» (mockups `BudA`, `BudMany`): month switch, the month total, filter chips on a long list,
 * then the envelopes in one card — folders with their inner limits, which fold. A tap opens the limit editor.
 */
@Composable
fun BudgetsScreen(state: BudgetsUiState, actions: BudgetsActions) {
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.budgets_title), onBack = actions.onBack) {
                CoShellIconButton(onClick = { actions.onEdit(null) }) {
                    Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.budgets_new))
                }
            }
        },
    ) { padding ->
        val month = state.month ?: return@Scaffold
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = Spacing.xl)) {
            item(key = "month") {
                MonthSwitch(month, state.periodDays, actions.onPreviousMonth, actions.onNextMonth, Modifier.padding(horizontal = Spacing.s))
            }
            if (state.isEmpty) {
                item(key = "empty") { EmptyBudgets(onNew = { actions.onEdit(null) }) }
                return@LazyColumn
            }
            state.summary?.let { summary ->
                item(key = "summary") { BudgetSummaryCard(summary, Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
            }
            if (state.showFilter) {
                item(key = "filter") {
                    BudgetFilterChips(state.counts, state.filter, actions.onFilterChange, Modifier.padding(top = Spacing.s))
                }
            } else {
                item(key = "title") { SectionTitle(stringResource(R.string.budgets_envelopes, state.total)) }
            }
            item(key = "envelopes") { Envelopes(state, actions, Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
            item(key = "hint") { CoShellHint(stringResource(R.string.budgets_hint), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) }
        }
    }
}

/**
 * All envelopes in one card: a family has tens of limits at most, so a plain column is cheaper than a lazy
 * list of card pieces and keeps the dividers simple.
 */
@Composable
private fun Envelopes(state: BudgetsUiState, actions: BudgetsActions, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        state.visible.forEachIndexed { index, envelope ->
            key(envelope.categoryId) {
                if (index > 0) Divider()
                val foldable = envelope.children.isNotEmpty()
                val expanded = envelope.categoryId !in state.collapsed
                EnvelopeRow(
                    envelope,
                    onClick = { actions.onEdit(envelope.categoryId) },
                    expanded = if (foldable) expanded else null,
                    onToggle = { actions.onToggleFolder(envelope.categoryId) },
                )
                if (foldable && expanded) {
                    envelope.children.forEach { child ->
                        key(child.categoryId) {
                            EnvelopeRow(child, onClick = { actions.onEdit(child.categoryId) }, inner = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Divider() = HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = Spacing.l))

@Composable
private fun EmptyBudgets(onNew: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(Spacing.l)) {
        Text(stringResource(R.string.budgets_empty_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.budgets_empty_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.s),
        )
        CoShellButton(onClick = onNew, modifier = Modifier.padding(top = Spacing.l)) { Text(stringResource(R.string.budgets_new)) }
    }
}
