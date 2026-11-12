package app.coshell.accounts.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.model.AccountType

private val cash = AccountsUiState.Row("1", "Наличные", AccountType.CASH, "85 000,00 ARS", isNegative = false, converted = "≈ 83,00 USD")
private val galicia = AccountsUiState.Row("2", "Galicia", AccountType.CARD, "1 250 000,00 ARS", false, color = 0xFFF26E22, converted = "≈ 1 220,00 USD")
private val credit = AccountsUiState.Row("3", "Кредитка", AccountType.CARD, "-15 000,00 RUB", isNegative = true, excludedFromTotal = true)

@Preview(showBackground = true)
@Composable
private fun AccountsScreenPreview() = CoShellTheme(darkTheme = false) { Accounts() }

@Preview(showBackground = true)
@Composable
private fun AccountsScreenDarkPreview() = CoShellTheme(darkTheme = true) { Accounts() }

@Composable
private fun Accounts() = AccountsScreen(state = accountsPreviewSample, actions = AccountsActions())

internal val accountsPreviewSample =
    AccountsUiState(
        active = listOf(galicia, credit, cash),
        total = "≈ 1 303,00 USD",
        groups = listOf(
            AccountsUiState.Group("TYPE:CARD", titleText = "Карты и банки", rows = listOf(galicia, credit), total = "≈ 1 220,00 USD", expanded = true),
            AccountsUiState.Group("TYPE:CASH", titleText = "Наличные", rows = listOf(cash), total = "≈ 83,00 USD", expanded = false),
        ),
        archived = listOf(AccountsUiState.Row("4", "Старая карта", AccountType.CARD, "0,00 RUB", false)),
    )
