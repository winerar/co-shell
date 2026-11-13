package app.coshell.accounts.edit.components

import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.labelRes
import app.coshell.core.designsystem.components.CoShellChoicePill
import app.coshell.core.model.AccountType
import app.coshell.core.designsystem.components.CoShellFormSection

/** «Тип» (#290): the account type as small pills — all five on one line (cash, card, deposit…) in a form card. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountTypeSection(selected: AccountType, onSelect: (AccountType) -> Unit) {
    CoShellFormSection(stringResource(R.string.account_type)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalArrangement = Arrangement.spacedBy(Spacing.s - Spacing.xxs)) {
            AccountType.entries.forEach { type ->
                CoShellChoicePill(stringResource(type.labelRes), selected == type, onClick = { onSelect(type) })
            }
        }
    }
}
