package app.coshell.currencies.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.currencies.list.AddCurrencyForm
import app.coshell.currencies.list.ReportCurrenciesActions

/** "Add a currency": chips of the family's currencies not in the list yet, plus a code field. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddCurrencyDialog(form: AddCurrencyForm, actions: ReportCurrenciesActions) {
    AlertDialog(
        onDismissRequest = actions.onDismissAdd,
        title = { Text(stringResource(R.string.report_currencies_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (form.suggestions.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        form.suggestions.forEach { code ->
                            CoShellFilterChip(
                                selected = form.code == code,
                                onClick = { actions.onCodeChange(code) },
                                label = { Text(code) },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = form.code,
                    onValueChange = actions.onCodeChange,
                    label = { Text(stringResource(R.string.account_currency_code)) },
                    isError = form.error,
                    supportingText = if (form.error) {
                        { Text(stringResource(R.string.account_currency_error)) }
                    } else {
                        null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { CoShellTextButton(onClick = actions.onConfirmAdd) { Text(stringResource(R.string.action_add)) } },
        dismissButton = { CoShellTextButton(onClick = actions.onDismissAdd) { Text(stringResource(R.string.action_cancel)) } },
    )
}
