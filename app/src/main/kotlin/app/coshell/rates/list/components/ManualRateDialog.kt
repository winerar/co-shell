package app.coshell.rates.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellDialog
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellSection
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.CodeKeyboard
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.rates.list.ManualRateActions
import app.coshell.rates.list.ManualRateForm
import app.coshell.ui.form.DateChips
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/** "1 [USD] costs [1 450] [ARS], from [date]" — adds or edits a manual rate. */
@Composable
internal fun ManualRateDialog(form: ManualRateForm, actions: ManualRateActions) {
    CoShellDialog(
        title = stringResource(R.string.rates_manual_title),
        confirmLabel = stringResource(R.string.action_save),
        onConfirm = actions.onSave,
        onDismiss = actions.onDismiss,
        dismissLabel = stringResource(R.string.action_cancel),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            CurrencyField(form.from, R.string.rates_manual_from, form.fromError, actions.onFromChange, Modifier.weight(1f))
            CurrencyField(form.to, R.string.rates_manual_to, form.toError, actions.onToChange, Modifier.weight(1f))
        }
        CoShellTextField(
            value = form.rate,
            onValueChange = actions.onRateChange,
            label = stringResource(R.string.rates_manual_rate),
            prefix = "1 ${form.from} = ",
            suffix = form.to,
            error = if (form.rateError) stringResource(R.string.rates_manual_rate_error) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = RateTextStyle,
        )
        CoShellSection(stringResource(R.string.rates_manual_date)) {
            DateChips(
                date = form.date,
                today = form.today,
                dateMillisUtc = form.date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
                onSelect = actions.onDateSelect,
                onPicked = actions.onDatePicked,
            )
            CoShellHint(stringResource(R.string.rates_manual_hint))
        }
    }
}

@Composable
private fun CurrencyField(
    value: String,
    label: Int,
    error: ManualRateForm.CurrencyError?,
    onChange: (String) -> Unit,
    modifier: Modifier,
) {
    CoShellTextField(
        value = value,
        onValueChange = onChange,
        label = stringResource(label),
        error = when (error) {
            null -> null
            ManualRateForm.CurrencyError.INVALID -> stringResource(R.string.rates_manual_currency_error)
            ManualRateForm.CurrencyError.SAME -> stringResource(R.string.rates_manual_same_error)
        },
        keyboardOptions = CodeKeyboard,
        modifier = modifier,
    )
}
