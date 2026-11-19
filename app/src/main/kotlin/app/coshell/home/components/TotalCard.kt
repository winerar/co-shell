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
import app.coshell.core.designsystem.components.CoShellFlipCard
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import androidx.compose.ui.semantics.Role
import app.coshell.core.designsystem.theme.AmountHeroStyle
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.HomeUiState
import app.coshell.ui.format.formatDayMonth

/**
 * "Overview" header (#519): the face is the total in the main currency (large) and the other report currencies, the rate date and
 * currencies left out, «Все счета ›»; a tap turns the card over — the back is the month (income, expenses, difference, daily average)
 * and the sums per account currency (the old «показать по валютам»). Without anything for the back the card stays one side.
 */
@Composable
internal fun TotalCard(state: HomeUiState, onAccounts: () -> Unit) {
    var flipped by rememberSaveable { mutableStateOf(false) }
    if (state.monthResult == null && state.byCurrency.isEmpty()) {
        CoShellCard(modifier = Modifier.fillMaxWidth(), accent = true) {
            Column(Modifier.padding(Spacing.l)) { TotalFront(state, onAccounts) }
        }
        return
    }
    CoShellFlipCard(
        flipped = flipped,
        onFlip = { flipped = !flipped },
        flipLabel = stringResource(R.string.flip_card_label),
        modifier = Modifier.fillMaxWidth(),
        front = { TotalFront(state, onAccounts) },
        back = { TotalBack(state) },
    )
}

@Composable
private fun TotalFront(state: HomeUiState, onAccounts: () -> Unit) {
    Text(stringResource(R.string.home_total_balance), style = MaterialTheme.typography.labelLarge)
    state.totals.forEachIndexed { index, total ->
        if (index == 0) {
            // The main total rolls its digits when it changes (design-questions §6).
            RollingAmountText(total, AmountHeroStyle, color = LocalHeroPalette.current.amount)
        } else {
            AmountLine(total, AmountTitleStyle, LocalHeroPalette.current.content)
        }
    }
    state.rateDate?.let { Caption(stringResource(R.string.home_total_rate_date, formatDayMonth(it))) }
    if (state.missing.isNotEmpty()) {
        Caption(stringResource(R.string.home_total_missing, state.missing.joinToString(", ")))
    }
    // «Счета» left the bottom bar for «Отчёты» (#140): the balance card leads to them.
    Text(
        stringResource(R.string.home_all_accounts),
        style = MaterialTheme.typography.labelLarge,
        color = LocalHeroPalette.current.content,
        modifier = Modifier.padding(top = Spacing.s).tactileClickable(Tactile.Control, role = Role.Button, onClick = onAccounts).padding(vertical = Spacing.xs),
    )
}

/** One line; a long amount shrinks instead of breaking inside the currency code. */
@Composable
internal fun AmountLine(text: String, style: TextStyle, color: Color) {
    AmountText(text = text, style = style, color = color, minFontSize = 16.sp)
}

/** Rate date, missing currencies, "tap for details" — grey hint under the amounts. */
@Composable
internal fun Caption(text: String) = CoShellHint(text, Modifier.padding(top = Spacing.xs))
