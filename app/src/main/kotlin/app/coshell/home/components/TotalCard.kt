package app.coshell.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.amount.RollingAmountText
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.theme.AmountHeroStyle
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.HomeUiState
import app.coshell.ui.format.formatDayMonth

/**
 * "Overview" header: total of all accounts in the main currency (large) and the other report
 * currencies, the rate date and currencies left out. Tap shows sums per account currency.
 */
@Composable
internal fun TotalCard(state: HomeUiState) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val canExpand = state.byCurrency.isNotEmpty()
    CoShellCard(
        modifier = Modifier.fillMaxWidth(),
        accent = true,
        onClick = if (canExpand) ({ expanded = !expanded }) else null,
    ) {
        Column(Modifier.padding(Spacing.l)) {
            Text(stringResource(R.string.home_total_balance), style = MaterialTheme.typography.labelLarge)
            state.totals.forEachIndexed { index, total ->
                if (index == 0) {
                    // The main total rolls its digits when it changes (design-questions §6).
                    RollingAmountText(total, AmountHeroStyle, color = MaterialTheme.colorScheme.primary)
                } else {
                    AmountLine(total, AmountTitleStyle, MaterialTheme.colorScheme.onSurface)
                }
            }
            state.rateDate?.let { Caption(stringResource(R.string.home_total_rate_date, formatDayMonth(it))) }
            if (state.missing.isNotEmpty()) {
                Caption(stringResource(R.string.home_total_missing, state.missing.joinToString(", ")))
            }
            if (canExpand) {
                Caption(stringResource(if (expanded) R.string.home_total_hide_breakdown else R.string.home_total_show_breakdown))
            }
            if (expanded) {
                state.byCurrency.forEach { AmountLine(it, AmountTextStyle, MaterialTheme.colorScheme.onSurface) }
            }
        }
    }
}

/** One line; a long amount shrinks instead of breaking inside the currency code. */
@Composable
private fun AmountLine(text: String, style: TextStyle, color: Color) {
    AmountText(text = text, style = style, color = color, minFontSize = 16.sp)
}

/** Rate date, missing currencies, "tap for details" — grey hint under the amounts. */
@Composable
private fun Caption(text: String) = CoShellHint(text, Modifier.padding(top = Spacing.xs))
