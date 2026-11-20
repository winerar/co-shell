package app.coshell.home.components

import app.coshell.core.designsystem.amount.shownAmount
import androidx.compose.foundation.layout.Arrangement
import app.coshell.core.designsystem.theme.RateSmallStyle
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.designsystem.charts.bulletColor
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.categoryName
import app.coshell.reports.review.MonthReviewUiState
import app.coshell.reports.review.components.changeColor
import app.coshell.ui.format.formatMonthOf

/**
 * «Итог октября» on «Обзор» in the first days of the month (board `Mon236A`, #236): what was left with the savings
 * rate, the three changes in one line, the limits kept. A tap opens the review; ✕ hides it until the next month.
 */
@Composable
internal fun MonthReviewCard(state: MonthReviewUiState, onOpen: () -> Unit, onHide: () -> Unit, modifier: Modifier = Modifier) {
    val month = state.month ?: return
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    CoShellCard(modifier.fillMaxWidth(), onClick = onOpen) {
        Row(Modifier.padding(start = Spacing.l, end = Spacing.xs, top = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.review_title, formatMonthOf(month)),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            CoShellIconButton(onClick = onHide) {
                Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.review_card_hide), tint = muted)
            }
        }
        Row(
            Modifier.padding(horizontal = Spacing.l),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            AmountText(
                "${state.saved} ${state.currency}",
                AmountTitleStyle,
                color = bulletColor(if (state.overspent) BulletTone.DANGER else BulletTone.NORMAL),
                minFontSize = 16.sp,
            )
            Text(cardRate(state), style = MaterialTheme.typography.bodySmall, color = muted, modifier = Modifier.padding(bottom = Spacing.xs))
        }
        if (state.changes.isNotEmpty()) {
            val colors = state.changes.map { changeColor(it.more) }
            val names = state.changes.map { categoryName(it.name) }
            val differences = state.changes.map { shownAmount(it.difference) }
            Text(
                buildAnnotatedString {
                    state.changes.forEachIndexed { index, _ ->
                        if (index > 0) append(" · ")
                        withStyle(SpanStyle(color = colors[index])) { append("${names[index]} ${differences[index]}") }
                    }
                },
                style = RateSmallStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.s),
            )
        }
        Row(Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.s, bottom = Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            Text(
                state.limits?.let { stringResource(R.string.review_card_limits, it.within, it.total) }.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = muted,
                modifier = Modifier.weight(1f),
            )
            Text(stringResource(R.string.review_card_more), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** «остаток · 36% дохода»; without income or spent more — the same words as the review screen. */
@Composable
private fun cardRate(state: MonthReviewUiState): String {
    val rate = state.savingsRate
    return when {
        rate == null -> stringResource(R.string.review_no_income)
        state.overspent -> stringResource(R.string.review_over_income)
        else -> stringResource(R.string.review_card_rate, rate)
    }
}
