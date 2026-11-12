package app.coshell.budgets.detail

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
import app.coshell.budgets.detail.components.EnvelopeHead
import app.coshell.budgets.detail.components.FolderCategories
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.components.TransactionRow
import app.coshell.ui.format.formatMonth
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar

/**
 * «Дом · ноябрь» (mockup `BudDetail`, #183): what is left, the note, the folder's categories, the month's operations.
 * The note icon in the bar opens the limit editor; a tap on an operation opens it for editing.
 */
@Composable
fun BudgetDetailScreen(state: BudgetDetailUiState, actions: BudgetDetailActions, categoryId: String) {
    val monthName = state.month?.let { formatMonth(it.firstDay) }.orEmpty()
    Scaffold(
        topBar = {
            val title = if (state.name.isEmpty()) "" else stringResource(R.string.budget_detail_title, state.name, monthName)
            ScreenTopBar(title, onBack = actions.onBack) {
                if (state.head != null) {
                    CoShellIconButton(onClick = { actions.onEditLimit(categoryId) }) {
                        Icon(
                            painterResource(DesignR.drawable.ic_description),
                            stringResource(R.string.budget_detail_edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        val head = state.head
        if (head == null) {
            NoLimit(Modifier.padding(padding), onSet = { actions.onEditLimit(categoryId) })
            return@Scaffold
        }
        val side = Modifier.padding(horizontal = Spacing.l)
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = Spacing.xl)) {
            item(key = "head") { EnvelopeHead(head, side.padding(vertical = Spacing.s)) }
            state.note?.let { note -> item(key = "note") { NoteCard(note, side.padding(vertical = Spacing.s)) } }
            if (state.categories.isNotEmpty()) {
                item(key = "categoriesTitle") { SectionTitle(stringResource(R.string.budget_detail_categories)) }
                item(key = "categories") {
                    FolderCategories(state.categories, actions.onOpenEnvelope, actions.onEditLimit, side.padding(vertical = Spacing.s))
                }
            }
            item(key = "operationsTitle") { SectionTitle(stringResource(R.string.budget_detail_operations, monthName)) }
            if (state.operations.isEmpty()) {
                item(key = "noOperations") { CoShellHint(stringResource(R.string.budget_detail_no_operations), side.padding(vertical = Spacing.s)) }
            } else {
                item(key = "operations") { Operations(state, actions, side.padding(vertical = Spacing.s)) }
            }
        }
    }
}

/**
 * The month's operations in one card. A month of one envelope is tens of rows at most, so a plain column (as on
 * «Бюджеты») keeps the card and its dividers simple.
 */
@Composable
private fun Operations(state: BudgetDetailUiState, actions: BudgetDetailActions, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        state.operations.forEachIndexed { index, row ->
            key(row.id) {
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = Spacing.l))
                TransactionRow(row, onClick = actions.onTransactionClick)
            }
        }
    }
}

/** «Заметка» and the text written in the editor. */
@Composable
private fun NoteCard(note: String, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l)) {
            Text(stringResource(R.string.budget_detail_note), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(note, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xs))
        }
    }
}

@Composable
private fun NoLimit(modifier: Modifier, onSet: () -> Unit) {
    Column(modifier.fillMaxWidth().padding(Spacing.l)) {
        Text(stringResource(R.string.budget_detail_no_limit_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.budget_detail_no_limit_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.s),
        )
        CoShellButton(onClick = onSet, modifier = Modifier.padding(top = Spacing.l)) { Text(stringResource(R.string.budget_detail_set_limit)) }
    }
}
