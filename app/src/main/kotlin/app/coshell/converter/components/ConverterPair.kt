package app.coshell.converter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.converter.ConverterActions
import app.coshell.converter.ConverterSide
import app.coshell.converter.ConverterUiState
import app.coshell.core.designsystem.components.CoShellAmountSlot
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellTagMoreChip
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * «Отдаю / Получаю» (board `Conv2PairType`): two amount slots, the keypad types in the active one; a tap on the other makes it
 * active, the pill opens the currency list, «⇅» swaps the currencies. Under them — the rate of the pair.
 */
@Composable
internal fun ConverterPair(state: ConverterUiState, actions: ConverterActions, onPick: (ConverterSide) -> Unit, modifier: Modifier = Modifier) {
    val muted = if (state.isEmpty) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Slot(ConverterSide.FROM, R.string.converter_give, state, actions, onPick, muted)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CoShellIconButton(onClick = actions.onSwap) {
                Icon(painterResource(DesignR.drawable.ic_sync_alt), stringResource(R.string.converter_swap), Modifier.rotate(90f))
            }
        }
        Slot(ConverterSide.TO, R.string.converter_get, state, actions, onPick, muted)
        if (state.rate != null) CoShellHint(stringResource(R.string.converter_rate, state.from, state.rate, state.to))
    }
}

@Composable
private fun Slot(
    side: ConverterSide,
    label: Int,
    state: ConverterUiState,
    actions: ConverterActions,
    onPick: (ConverterSide) -> Unit,
    color: Color,
) {
    val code = if (side == ConverterSide.FROM) state.from else state.to
    val text = if (side == ConverterSide.FROM) state.fromText else state.toText
    CoShellAmountSlot(
        text = text.orEmpty(),
        onClick = { actions.onActivate(side) },
        label = stringResource(label),
        trailing = { CoShellTagMoreChip(stringResource(R.string.converter_currency, code), onClick = { onPick(side) }) },
        active = state.active == side,
        message = if (text == null) stringResource(R.string.converter_no_rate) else null,
        main = true,
        color = color,
    )
}
