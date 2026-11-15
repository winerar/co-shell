package app.coshell.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.charts.CoShellBarRow
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState.Bar
import app.coshell.reports.ReportsUiState.Spending
import app.coshell.reports.categoryName
import app.coshell.reports.comparisonText
import app.coshell.reports.isMore

/**
 * «На что ушли деньги» (board `RepA`): the total in the report currency, the line against the period before, the
 * largest top-level categories as bars with the previous period's grey mark, «Другое» and «Все категории».
 */
@Composable
internal fun SpendingCard(
    spending: Spending,
    currency: String,
    ghostCaption: String?,
    onAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CoShellCard(modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.reports_spending_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            ghostCaption?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Row(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.xs), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(spending.total, style = AmountTitleStyle)
            Text(currency, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = Spacing.xxs))
        }
        spending.comparison?.let { comparison ->
            val status = LocalStatusPalette.current
            Text(
                comparisonText(comparison),
                style = MaterialTheme.typography.bodySmall,
                color = if (comparison.isMore) status.warning else status.success,
                modifier = Modifier.padding(horizontal = Spacing.l),
            )
        }
        spending.bars.forEach { bar -> SpendingBar(bar, Modifier.padding(horizontal = Spacing.l)) }
        spending.other?.let { other ->
            val name = categoryName(other.firstName)
            val head = if (other.more > 0) stringResource(R.string.reports_other_more, name, other.more) else stringResource(R.string.reports_other, name)
            Text(
                "$head · ${other.value} · ${other.percent}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.xs),
            )
        }
        Text(
            stringResource(R.string.reports_all, spending.categories.size),
            style = MaterialTheme.typography.labelLarge,
            color = accentInk(),
            modifier = Modifier
                .fillMaxWidth()
                .tactileClickable(onClick = onAll)
                .padding(horizontal = Spacing.l, vertical = Spacing.m),
        )
    }
}

@Composable
internal fun SpendingBar(bar: Bar, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    CoShellBarRow(
        label = categoryName(bar.name),
        value = bar.value,
        share = "${bar.percent}%",
        fraction = bar.fraction,
        color = bar.color?.let { categoryColor(it) } ?: MaterialTheme.colorScheme.primary,
        ghost = bar.ghost,
        ghostDescription = bar.ghostValue?.let { stringResource(R.string.reports_ghost_value, it) },
        leading = { CategoryBadge(bar.icon, bar.color, size = Sizes.badgeSmall) },
        onClick = onClick,
        modifier = modifier,
    )
}
