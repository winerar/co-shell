package app.coshell.budgets.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import app.coshell.R
import app.coshell.budgets.list.BudgetsUiState.FirstLimits
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellCheckMark
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.list.SectionTitle

/**
 * The first visit to «Бюджеты», while there are no limits (#185, board `BudFirst`): a hero line, the limits made from the earlier months
 * with a tick on each, «Создать N лимитов» and a word on when the month starts. Amounts are edited afterwards, as any limit.
 */
@Composable
internal fun FirstLimits(
    offer: FirstLimits,
    startDay: Int,
    onToggle: (String) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), tone = CardTone.Hero) {
            Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(stringResource(R.string.budgets_first_title), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.budgets_first_text), style = MaterialTheme.typography.bodyMedium)
            }
        }
        SectionTitle(stringResource(R.string.budgets_first_label))
        CoShellRowGroup(Modifier.padding(horizontal = Spacing.l)) {
            offer.rows.forEach { row ->
                row {
                    CoShellListRow(
                        title = row.name,
                        subtitle = stringResource(R.string.budgets_first_average),
                        leading = { CategoryBadge(row.icon, row.color) },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                                AmountText(row.amount, AmountTextStyle)
                                CoShellCheckMark(if (row.checked) ToggleableState.On else ToggleableState.Off)
                            }
                        },
                        onClick = { onToggle(row.categoryId) },
                    )
                }
            }
        }
        val count = offer.checkedCount
        CoShellButton(
            onClick = onCreate,
            enabled = count > 0,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l),
        ) { Text(pluralStringResource(R.plurals.budgets_first_create, count, count)) }
        CoShellHint(stringResource(R.string.budgets_first_note, startDay), Modifier.padding(horizontal = Spacing.l))
    }
}
