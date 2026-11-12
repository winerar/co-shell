package app.coshell.budgets.edit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.edit.BudgetEditUiState
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.tokens.Sizes

/**
 * «Внутри папки · необязательно» (`BudEdit`): each subcategory with its own limit or «без своего лимита» and
 * «＋ лимит». A tap opens that subcategory's editor; its limit is a part of the folder's, not added on top.
 */
@Composable
internal fun InsideFolder(rows: List<BudgetEditUiState.Inside>, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        rows.forEach { row ->
            CoShellListRow(
                title = row.name,
                subtitle = row.limit?.let { stringResource(R.string.budget_inside_limit, it) } ?: stringResource(R.string.budget_inside_none),
                leading = { CategoryBadge(row.icon, row.color, size = Sizes.badgeSmall) },
                trailing = {
                    if (row.limit == null) {
                        Text(stringResource(R.string.budget_inside_add), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    } else {
                        Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null)
                    }
                },
                onClick = { onOpen(row.categoryId) },
            )
        }
    }
}
