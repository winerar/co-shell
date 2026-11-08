package app.coshell.accounts.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.model.AccountType

@Preview(showBackground = true)
@Composable
private fun AccountEditNewPreview() {
    CoShellTheme {
        AccountEditScreen(
            AccountEditUiState(
                currency = "ARS",
                suggestedCurrencies = AccountEditViewModel.DEFAULT_CURRENCIES,
                nameError = true,
            ),
            AccountEditActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountEditExistingPreview() {
    CoShellTheme {
        AccountEditScreen(
            AccountEditUiState(
                isNew = false,
                name = "Наличные",
                currency = "USD",
                type = AccountType.CASH,
                initialBalance = "1250,5",
                currencyLocked = true,
            ),
            AccountEditActions(),
        )
    }
}
