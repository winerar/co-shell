package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption

/** One chip per account: "Карта · ₽". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountChips(accounts: List<AccountOption>, selectedId: String?, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        accounts.forEach { account ->
            FilterChip(
                selected = account.id == selectedId,
                onClick = { onSelect(account.id) },
                label = { Text("${account.name} · ${account.currencySymbol}") },
            )
        }
    }
}
