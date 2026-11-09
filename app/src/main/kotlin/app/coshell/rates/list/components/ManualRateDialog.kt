package app.coshell.rates.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.rates.list.ManualRateActions
import app.coshell.rates.list.ManualRateForm
import app.coshell.ui.form.DateChips
import app.coshell.ui.form.FormHint
import app.coshell.ui.form.FormSection
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/** "1 [USD] costs [1 450] [ARS], from [date]" — adds or edits a manual rate. */
@Composable
internal fun ManualRateDialog(form: ManualRateForm, actions: ManualRateActions) {
    AlertDialog(
        onDismissRequest = actions.onDismiss,
        title = { Text(stringResource(R.string.rates_manual_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyField(form.from, R.string.rates_manual_from, form.fromError, actions.onFromChange, Modifier.weight(1f))
                    CurrencyField(form.to, R.string.rates_manual_to, form.toError, actions.onToChange, Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = form.rate,
                    onValueChange = actions.onRateChange,
                    label = { Text(stringResource(R.string.rates_manual_rate)) },
                    prefix = { Text("1 ${form.from} = ") },
                    suffix = { Text(form.to) },
                    isError = form.rateError,
                    supportingText = if (form.rateError) {
                        { Text(stringResource(R.string.rates_manual_rate_error)) }
                    } else {
                        null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                FormSection(stringResource(R.string.rates_manual_date)) {
                    DateChips(
                        date = form.date,
                        today = form.today,
                        dateMillisUtc = form.date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
                        onSelect = actions.onDateSelect,
                        onPicked = actions.onDatePicked,
                    )
                    FormHint(stringResource(R.string.rates_manual_hint))
                }
            }
        },
        confirmButton = { CoShellTextButton(onClick = actions.onSave) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { CoShellTextButton(onClick = actions.onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun CurrencyField(
    value: String,
    label: Int,
    error: ManualRateForm.CurrencyError?,
    onChange: (String) -> Unit,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        isError = error != null,
        supportingText = when (error) {
            null -> null
            ManualRateForm.CurrencyError.INVALID -> { { Text(stringResource(R.string.rates_manual_currency_error)) } }
            ManualRateForm.CurrencyError.SAME -> { { Text(stringResource(R.string.rates_manual_same_error)) } }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        modifier = modifier,
    )
}
