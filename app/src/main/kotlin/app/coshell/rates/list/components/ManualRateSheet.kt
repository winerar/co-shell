package app.coshell.rates.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.components.CoShellSection
import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.components.keypad.CoShellKeypad
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.designsystem.theme.AmountInputStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.CurrencyCode
import app.coshell.rates.list.ManualRateActions
import app.coshell.rates.list.ManualRateForm
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.DateChips
import app.coshell.ui.sheet.ModalSheet
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import app.coshell.core.designsystem.R as DesignR

/**
 * «Курс вручную» (#345 — mockup `FbModalARate`): «1 [USD ▾] = [ARS ▾]», the rate in large figures typed on our
 * keypad (no system keyboard, #291), «Действует с» chips and the rule. The keypad's «Сохранить» saves; the
 * screen drops the sheet once the rate is stored.
 */
@Composable
internal fun ManualRateSheet(form: ManualRateForm, actions: ManualRateActions) {
    var picking by rememberSaveable { mutableStateOf<Side?>(null) }
    ModalSheet(title = stringResource(R.string.rates_manual_title), onClosed = actions.onDismiss) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text("1", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            CurrencyPill(form.from, form.fromError != null) { picking = Side.FROM }
            Text("=", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            CurrencyPill(form.to, form.toError != null) { picking = Side.TO }
        }
        currencyError(form)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
        RateFigure(form)
        CoShellSection(stringResource(R.string.rates_manual_date)) {
            DateChips(
                date = form.date,
                today = form.today,
                dateMillisUtc = form.date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
                onSelect = actions.onDateSelect,
                onPicked = actions.onDatePicked,
            )
        }
        CoShellHint(stringResource(R.string.rates_manual_hint))
        CoShellKeypad(
            onKey = { key ->
                if (key == KeypadKey.Done) actions.onSave() else actions.onRateChange(AmountKeys.apply(form.rate, key, ManualRateForm.RATE_FRACTION_DIGITS))
            },
            doneLabel = stringResource(R.string.action_save),
            backspaceLabel = stringResource(R.string.keypad_backspace),
            showPlus = false,
        )
    }
    picking?.let { side ->
        ChoiceSheet(
            title = stringResource(if (side == Side.FROM) R.string.rates_manual_from else R.string.rates_manual_to),
            options = form.choices.map { ChoiceOption(it, it) },
            selected = if (side == Side.FROM) form.from else form.to,
            onPick = {
                if (side == Side.FROM) actions.onFromChange(it) else actions.onToChange(it)
                picking = null
            },
            onDismiss = { picking = null },
            extra = { query -> CurrencyCode.parseOrNull(query)?.let { ChoiceOption(it.value, it.value) } },
        )
    }
}

private enum class Side { FROM, TO }

@Composable
private fun CurrencyPill(code: String, error: Boolean, onClick: () -> Unit) {
    CoShellOutlinedButton(onClick = onClick, color = if (error) MaterialTheme.colorScheme.error else null) {
        Text(code.ifEmpty { "—" }, style = MaterialTheme.typography.titleMedium)
        Icon(painterResource(DesignR.drawable.ic_keyboard_arrow_down), contentDescription = null)
    }
}

@Composable
private fun RateFigure(form: ManualRateForm) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text(
            "${AmountKeys.display(form.rate)} ${form.to}",
            style = AmountInputStyle,
            textAlign = TextAlign.End,
            color = if (form.rate.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
        if (form.rateError) {
            Text(stringResource(R.string.rates_manual_rate_error), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun currencyError(form: ManualRateForm): String? = when {
    form.fromError == ManualRateForm.CurrencyError.INVALID || form.toError == ManualRateForm.CurrencyError.INVALID ->
        stringResource(R.string.rates_manual_currency_error)
    form.toError == ManualRateForm.CurrencyError.SAME -> stringResource(R.string.rates_manual_same_error)
    else -> null
}
