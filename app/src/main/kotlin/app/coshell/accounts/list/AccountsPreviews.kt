package app.coshell.accounts.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.model.AccountType

@Preview(showBackground = true)
@Composable
private fun AccountsScreenPreview() {
    CoShellTheme {
        AccountsScreen(
            state = AccountsUiState(
                active = listOf(
                    AccountsUiState.Row("1", "Наличные", AccountType.CASH, "85 000,00 ARS", isNegative = false),
                    AccountsUiState.Row("2", "Кредитка", AccountType.CARD, "-15 000,00 RUB", isNegative = true, excludedFromTotal = true),
                ),
                archived = listOf(AccountsUiState.Row("3", "Старая карта", AccountType.CARD, "0,00 RUB", false)),
                showArchived = true,
            ),
            onAccountClick = {},
            onAddAccount = {},
            onToggleArchived = {},
        )
    }
}
