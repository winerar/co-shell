package app.coshell.accounts.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.accounts.labelRes
import app.coshell.core.model.AccountType
import app.coshell.ui.form.FormSection

/** Account type as a row of chips (cash, card, deposit…). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountTypeSection(selected: AccountType, onSelect: (AccountType) -> Unit) {
    FormSection(stringResource(R.string.account_type)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AccountType.entries.forEach { type ->
                FilterChip(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    label = { Text(stringResource(type.labelRes)) },
                )
            }
        }
    }
}
