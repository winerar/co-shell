package app.coshell.budgets.detail.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.detail.BudgetDetailUiState.Inner
import app.coshell.budgets.list.components.EnvelopeRow
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Categories of the folder in one card: one with its own limit is a row like on «Бюджеты» and opens its envelope;
 * one without shows what it took this month and opens the editor to give it a limit («＋ лимит»).
 */
@Composable
internal fun FolderCategories(
    categories: List<Inner>,
    onOpenEnvelope: (String) -> Unit,
    onAddLimit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    CoShellCard(modifier.fillMaxWidth()) {
        categories.forEachIndexed { index, inner ->
            key(inner.categoryId) {
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = Spacing.l))
                when (inner) {
                    is Inner.Limited -> EnvelopeRow(inner.envelope, onClick = { onOpenEnvelope(inner.categoryId) })
                    is Inner.Plain -> PlainRow(inner, onClick = { onAddLimit(inner.categoryId) })
                }
            }
        }
    }
}

@Composable
private fun PlainRow(inner: Inner.Plain, onClick: () -> Unit) {
    CoShellListRow(
        title = inner.name,
        subtitle = listOf(
            inner.spent?.let { stringResource(R.string.budget_detail_spent, it) } ?: stringResource(R.string.budgets_caption_idle),
            stringResource(R.string.budget_inside_none),
        ).joinToString(" · "),
        leading = { CategoryBadge(inner.icon, inner.color) },
        trailing = {
            Text(stringResource(R.string.budget_inside_add), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        },
        onClick = onClick,
    )
}
