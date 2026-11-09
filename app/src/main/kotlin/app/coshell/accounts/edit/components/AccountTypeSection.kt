package app.coshell.accounts.edit.components

import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.labelRes
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.model.AccountType
import app.coshell.core.designsystem.components.CoShellSection

/** Account type as a row of chips (cash, card, deposit…). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountTypeSection(selected: AccountType, onSelect: (AccountType) -> Unit) {
    CoShellSection(stringResource(R.string.account_type)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            AccountType.entries.forEach { type ->
                CoShellFilterChip(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    label = { Text(stringResource(type.labelRes)) },
                )
            }
        }
    }
}
