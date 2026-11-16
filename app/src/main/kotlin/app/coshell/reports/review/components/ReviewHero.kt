package app.coshell.reports.review.components

import androidx.compose.foundation.layout.Arrangement
import app.coshell.core.designsystem.theme.AmountReviewStyle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.review.MonthReviewUiState
import app.coshell.ui.format.formatMonth

/** «Остаток за октябрь · 750 USD · отложили 36% дохода», then «Потрачено» and «Пришло» side by side (board `Mon236Screen`). */
@Composable
internal fun ReviewHero(state: MonthReviewUiState, modifier: Modifier = Modifier) {
    val month = state.month ?: return
    CoShellCard(modifier.fillMaxWidth(), accent = true) {
        Column(Modifier.padding(Spacing.l)) {
            Text(stringResource(R.string.review_left, formatMonth(month.firstDay)), style = MaterialTheme.typography.bodyMedium)
            FlowRow(
                Modifier.padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                itemVerticalAlignment = Alignment.Bottom,
            ) {
                AmountText(
                    "${state.saved} ${state.currency}",
                    AmountReviewStyle,
                    color = savedColor(state.overspent),
                    minFontSize = 20.sp,
                )
                Text(
                    rateText(state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.xs),
                )
            }
            Row(Modifier.padding(top = Spacing.m), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Figure(stringResource(R.string.review_spent), "${state.spent} ${state.currency}", Modifier.weight(1f))
                Figure(stringResource(R.string.review_earned), "${state.earned} ${state.currency}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Figure(title: String, amount: String, modifier: Modifier) {
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AmountText(amount, AmountTextStyle, color = MaterialTheme.colorScheme.onSurface, minFontSize = 12.sp)
    }
}

/** Left over — the hero's amount colour; spent more than came in — the expense one. */
@Composable
internal fun savedColor(overspent: Boolean) = if (overspent) LocalAmountPalette.current.expense else LocalHeroPalette.current.amount

/** «отложили 36% дохода», «траты больше дохода», «дохода нет — без %». */
@Composable
internal fun rateText(state: MonthReviewUiState): String {
    val rate = state.savingsRate
    return when {
        rate == null -> stringResource(R.string.review_no_income)
        state.overspent -> stringResource(R.string.review_over_income)
        else -> stringResource(R.string.review_rate, rate)
    }
}
