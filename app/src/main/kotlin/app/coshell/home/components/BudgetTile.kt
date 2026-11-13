package app.coshell.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.budgets.list.BudgetsUiState
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.amount.splitAmount
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.designsystem.charts.CoShellBullet
import app.coshell.core.designsystem.components.CoShellTile
import app.coshell.core.designsystem.charts.bulletColor
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Spacing

/**
 * «Бюджет · 19 дней» (board `BudHome`): what is left of the month's limits in the main currency, a thin bullet with
 * today's mark under the code, as the expenses tile has it. Ahead of pace or over — the tile takes the colour of the state; over — the number is the overspending.
 */
@Composable
internal fun BudgetTile(summary: BudgetsUiState.Summary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hot = summary.tone == BulletTone.WARNING || summary.tone == BulletTone.DANGER
    val title = summary.daysLeft?.let { pluralStringResource(R.plurals.home_budget_title_days, it, it) }
        ?: stringResource(R.string.home_budget_title)
    CoShellTile(title = title, onClick = onClick, tint = if (hot) bulletColor(summary.tone) else null, modifier = modifier) {
        val (number, code) = splitAmount(summary.left)
        AmountText(
            number,
            AmountTitleStyle,
            color = if (summary.over) bulletColor(BulletTone.DANGER) else MaterialTheme.colorScheme.onSurface,
            minFontSize = 14.sp,
        )
        val caption = listOfNotNull(code, stringResource(R.string.home_budget_over).takeIf { summary.over })
        Text(
            caption.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = if (summary.over) bulletColor(BulletTone.DANGER) else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        CoShellBullet(summary.fraction, summary.tone, Modifier.padding(top = Spacing.s), mark = summary.pace)
    }
}
