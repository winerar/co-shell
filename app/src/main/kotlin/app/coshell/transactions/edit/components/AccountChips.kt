package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption

/** One chip per account: "Карта · ₽". */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountChips(accounts: List<AccountOption>, selectedId: String?, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        accounts.forEach { account ->
            CoShellFilterChip(
                selected = account.id == selectedId,
                onClick = { onSelect(account.id) },
                label = { Text("${account.name} · ${account.currencySymbol}") },
            )
        }
    }
}
