package app.coshell.reports.review.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import app.coshell.core.designsystem.components.CoShellDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.list.components.EnvelopeRow
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.designsystem.charts.bulletColor
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.review.MonthReviewUiState.Limits

/**
 * «Лимиты» of the review (board `Mon236Screen`): «Уложились 4 из 6» with a square per limit, the overspent ones as on
 * «Бюджеты», then «Все лимиты октября ›». A tap on a limit opens its envelope in that month.
 */
@Composable
internal fun LimitsCard(limits: Limits, allTitle: String, onAll: () -> Unit, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        LimitsTally(limits)
        limits.over.forEach { envelope ->
            key(envelope.categoryId) {
                CoShellDivider()
                EnvelopeRow(envelope, onClick = { onOpen(envelope.categoryId) })
            }
        }
        CoShellDivider()
        Row(
            Modifier.fillMaxWidth().tactileClickable(onClick = onAll).padding(horizontal = Spacing.l, vertical = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(allTitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/** «Уложились 4 из 6 · перерасход · 2» and one square per limit: kept first, overspent after. */
@Composable
internal fun LimitsTally(limits: Limits, modifier: Modifier = Modifier) {
    val over = limits.total - limits.within
    Column(modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.m)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                stringResource(R.string.review_limits_kept, limits.within, limits.total),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            if (over > 0) {
                Text(
                    stringResource(R.string.review_limits_over, over),
                    style = MaterialTheme.typography.bodySmall,
                    color = bulletColor(BulletTone.DANGER),
                )
            }
        }
        val kept = bulletColor(BulletTone.NORMAL)
        val danger = bulletColor(BulletTone.DANGER)
        Row(Modifier.fillMaxWidth().padding(top = Spacing.s), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            repeat(limits.total) { index ->
                Box(Modifier.weight(1f).height(Spacing.s).background(if (index < limits.within) kept else danger, RoundedCornerShape(Spacing.xs)))
            }
        }
    }
}
