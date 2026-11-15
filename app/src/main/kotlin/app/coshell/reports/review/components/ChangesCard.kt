package app.coshell.reports.review.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.categoryName
import app.coshell.reports.review.MonthReviewUiState.Change

/**
 * «Главное к среднему за 3 мес.» (board `Mon236Screen`): up to three folders that spent noticeably more or less than
 * usual — «Подарки · 120 · обычно 30 · +90 · +300%». A tap opens «Из чего сложилось» of the month (#188).
 */
@Composable
internal fun ChangesCard(changes: List<Change>, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        changes.forEachIndexed { index, change ->
            key(change.categoryId) {
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ChangeRow(change, onClick = { onOpen(change.categoryId) })
            }
        }
    }
}

@Composable
private fun ChangeRow(change: Change, onClick: () -> Unit) {
    val color = changeColor(change.more)
    Row(
        Modifier.fillMaxWidth().tactileClickable(onClick = onClick).padding(horizontal = Spacing.l, vertical = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        CategoryBadge(change.icon, change.color, size = Sizes.badgeSmall)
        Column(Modifier.weight(1f)) {
            Text(categoryName(change.name), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(R.string.review_change_usual, change.amount, change.usual),
                style = RateTextStyle.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(change.difference, style = RateTextStyle, color = color, maxLines = 1)
            change.percent?.let { Text(signedPercent(it), style = MaterialTheme.typography.bodySmall, color = color) }
        }
        Icon(
            painterResource(DesignR.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** More than usual — the expense colour, less — the calm one (as the comparison line of «Отчётов»). */
@Composable
internal fun changeColor(more: Boolean): Color = if (more) LocalAmountPalette.current.expense else LocalStatusPalette.current.success

internal fun signedPercent(percent: Int): String = if (percent > 0) "+$percent%" else "$percent%"
