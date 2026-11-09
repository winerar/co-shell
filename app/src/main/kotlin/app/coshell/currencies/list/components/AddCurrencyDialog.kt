package app.coshell.currencies.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import app.coshell.R
import app.coshell.core.designsystem.components.CodeKeyboard
import app.coshell.core.designsystem.components.CoShellDialog
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.currencies.list.AddCurrencyForm
import app.coshell.currencies.list.ReportCurrenciesActions

/** "Add a currency": chips of the family's currencies not in the list yet, plus a code field. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddCurrencyDialog(form: AddCurrencyForm, actions: ReportCurrenciesActions) {
    CoShellDialog(
        title = stringResource(R.string.report_currencies_add),
        confirmLabel = stringResource(R.string.action_add),
        onConfirm = actions.onConfirmAdd,
        onDismiss = actions.onDismissAdd,
        dismissLabel = stringResource(R.string.action_cancel),
    ) {
        if (form.suggestions.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                form.suggestions.forEach { code ->
                    CoShellFilterChip(
                        selected = form.code == code,
                        onClick = { actions.onCodeChange(code) },
                        label = { Text(code) },
                    )
                }
            }
        }
        CoShellTextField(
            value = form.code,
            onValueChange = actions.onCodeChange,
            label = stringResource(R.string.account_currency_code),
            error = if (form.error) stringResource(R.string.account_currency_error) else null,
            keyboardOptions = CodeKeyboard.copy(imeAction = ImeAction.Done),
        )
    }
}
