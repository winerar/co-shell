package app.coshell.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.components.FlipCueClearance
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.HomeUiState
import app.coshell.ui.format.sentenceAmounts
import app.coshell.ui.format.formatMonth

/**
 * The back of the «Обзор» hero card (#519, poll 2026-11-19, variant А): the month in the main currency — income, expenses,
 * difference, the daily average — and below it the sums per account currency. Blocks without data are left out.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColumnScope.TotalBack(state: HomeUiState) {
    val hero = LocalHeroPalette.current
    val month = state.spending?.month
    if (month != null) {
        // The top line keeps clear of the flip «↻» in the corner.
        Text(
            stringResource(R.string.home_back_title, formatMonth(month)),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(end = FlipCueClearance),
        )
        val result = state.monthResult
        FlowRow(
            Modifier.padding(top = Spacing.s),
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            state.income?.totals?.firstOrNull()?.let { Figure(R.string.home_back_income, it, hero.amount) }
            state.spending?.totals?.firstOrNull()?.let { Figure(R.string.home_back_spending, it, hero.expense) }
            result?.let { Figure(R.string.home_back_result, it.result, if (it.positive) hero.amount else hero.expense) }
        }
        result?.let {
            Text(
                stringResource(R.string.home_back_per_day, shownAmount(it.perDay)).sentenceAmounts(),
                style = MaterialTheme.typography.bodySmall,
                color = hero.hint,
                modifier = Modifier.padding(top = Spacing.s),
            )
        }
    }
    if (state.byCurrency.isNotEmpty()) {
        if (month != null) CoShellDivider(Modifier.padding(vertical = Spacing.m))
        Text(
            stringResource(R.string.home_back_currencies),
            style = MaterialTheme.typography.bodySmall,
            color = hero.hint,
            modifier = if (month == null) Modifier.padding(end = FlipCueClearance) else Modifier,
        )
        state.byCurrency.forEach { AmountLine(it, AmountTextStyle, hero.content) }
    }
}

@Composable
private fun Figure(label: Int, value: String, color: Color) {
    Column {
        Text(stringResource(label), style = MaterialTheme.typography.bodySmall, color = LocalHeroPalette.current.hint)
        AmountText(value, AmountTitleStyle, color = color, minFontSize = 14.sp)
    }
}
