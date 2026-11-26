package app.coshell.goals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.tokens.Spacing

/** Icon key of a goal without its own icon: the piggy bank of the category icon set. */
internal const val GOAL_ICON = "interest"

/** «К 1 июля · ≈ 125 USD в месяц», «Цель достигнута»… — the second line of a goal (board `GoalListA`). */
@Composable
internal fun goalWhenText(row: GoalRowUi): String = when (row.whenKind) {
    GoalWhen.REACHED -> stringResource(R.string.goals_reached)
    GoalWhen.OVERDUE -> stringResource(R.string.goals_overdue, row.left)
    GoalWhen.NO_DEADLINE -> stringResource(R.string.goals_no_deadline, row.left)
    GoalWhen.DEADLINE -> stringResource(R.string.goals_until, row.deadline, row.perMonth)
    GoalWhen.YEARLY -> stringResource(R.string.goals_yearly_until, row.deadline, row.perMonth)
}

/** One goal: badge, name, when and how much a month, the meter; saved «из» target on the right. Same on «Цели» and «Обзоре». */
@Composable
internal fun GoalRow(row: GoalRowUi, onClick: () -> Unit, meter: Boolean = true) {
    CoShellListRow(
        title = { Text(row.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supporting = {
            Column {
                Text(goalWhenText(row), maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (meter) {
                    Spacer(Modifier.height(Spacing.xs))
                    CoShellMeter(row.fraction, MaterialTheme.colorScheme.primary, Modifier.fillMaxWidth())
                }
            }
        },
        leading = { CategoryBadge(row.icon ?: GOAL_ICON, row.color ?: CategoryPalette.forKey(row.id)) },
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                AmountText(row.saved, AmountTextStyle)
                Text(stringResource(R.string.goals_of, row.target), style = MaterialTheme.typography.bodySmall)
            }
        },
        onClick = onClick,
    )
}
