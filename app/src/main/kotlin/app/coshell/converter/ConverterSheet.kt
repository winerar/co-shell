package app.coshell.converter

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.converter.components.ConverterAll
import app.coshell.converter.components.ConverterPair
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellModalSheetLayout
import app.coshell.core.designsystem.components.keypad.CoShellKeypad
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ConverterMode
import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.ChoiceSheet
import app.coshell.core.designsystem.R as DesignR

/** Everything the converter sheet can do; the route fills it from the view model. */
internal data class ConverterActions(
    val onKey: (KeypadKey) -> Unit = {},
    val onActivate: (ConverterSide) -> Unit = {},
    val onSwap: () -> Unit = {},
    val onPick: (ConverterSide, String) -> Unit = { _, _ -> },
    val onSource: (String) -> Unit = {},
    val onMode: (ConverterMode) -> Unit = {},
)

/**
 * The converter (#319, boards `Conv2*`): «Пара» (default) or «Все валюты», the button before the × switches; the number is typed on
 * the own keypad below, «Готово» closes. The inside of the sheet — its window is the navigation dialog.
 */
@Composable
internal fun ConverterSheetBody(state: ConverterUiState, actions: ConverterActions, onClose: () -> Unit) {
    var picking by remember { mutableStateOf<ConverterSide?>(null) }
    val pair = state.mode == ConverterMode.PAIR
    CoShellModalSheetLayout(
        title = stringResource(R.string.converter_title),
        closeLabel = stringResource(R.string.action_close),
        onClose = onClose,
        subtitle = stringResource(if (pair) R.string.converter_subtitle_pair else R.string.converter_subtitle_all),
        headerAction = {
            CoShellIconButton(onClick = { actions.onMode(if (pair) ConverterMode.ALL else ConverterMode.PAIR) }) {
                Icon(
                    painterResource(if (pair) DesignR.drawable.ic_format_list_bulleted else DesignR.drawable.ic_sync_alt),
                    stringResource(if (pair) R.string.converter_to_all else R.string.converter_to_pair),
                )
            }
        },
    ) {
        if (pair) ConverterPair(state, actions, onPick = { picking = it }) else ConverterAll(state, actions)
        CoShellKeypad(
            onKey = { key -> if (key == KeypadKey.Done) onClose() else actions.onKey(key) },
            doneLabel = stringResource(R.string.keypad_done),
            backspaceLabel = stringResource(R.string.keypad_backspace),
            showDecimal = state.source.isNotEmpty() && Currencies.info(CurrencyCode(state.source)).fractionDigits > 0,
            modifier = Modifier.padding(bottom = Spacing.s),
        )
    }
    picking?.let { side ->
        ChoiceSheet(
            title = stringResource(if (side == ConverterSide.FROM) R.string.converter_pick_give else R.string.converter_pick_get),
            options = state.options.map { ChoiceOption(it, it) },
            selected = if (side == ConverterSide.FROM) state.from else state.to,
            onPick = { actions.onPick(side, it) },
            onDismiss = { picking = null },
        )
    }
}
