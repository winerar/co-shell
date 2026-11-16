package app.coshell.templates.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCheckTile
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.CodeKeyboard
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.templates.edit.TemplateEditActions
import app.coshell.templates.edit.TemplateEditUiState

/**
 * «Платёж в другой валюте» (#415, owner picked Б, board `Rec415B`): a check box under the amount, like «чек в другой
 * валюте» of the entry sheet. Ticked — currency chips (the accounts' and the template's) and a code field; the amount
 * above is then in that currency, and what leaves the account is entered when posting (poll 3 «Д»).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ForeignCurrencyBlock(state: TemplateEditUiState, actions: TemplateEditActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellCheckTile(
            state.foreign,
            stringResource(R.string.template_foreign_title),
            stringResource(R.string.template_foreign_subtitle),
            actions.onForeignToggle,
            Modifier.fillMaxWidth(),
        )
        CoShellExpandable(state.foreign) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                val choices = state.foreignChoices
                if (choices.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        choices.forEach { code ->
                            CoShellFilterChip(
                                selected = state.amountCurrency == code,
                                onClick = { actions.onAmountCurrencyChange(code) },
                                label = { Text(code) },
                            )
                        }
                    }
                }
                CoShellTextField(
                    value = state.amountCurrency.orEmpty(),
                    onValueChange = actions.onAmountCurrencyChange,
                    label = stringResource(R.string.transaction_receipt_currency),
                    error = stringResource(R.string.template_foreign_currency_error).takeIf { state.amountCurrencyError },
                    keyboardOptions = CodeKeyboard,
                )
            }
        }
    }
}
