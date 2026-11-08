package app.coshell.transactions.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.model.TransactionType
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem
import kotlinx.datetime.LocalDate

private val previewAccounts = listOf(
    AccountOption("card", "Карта", "ARS", "$"),
    AccountOption("cash", "Наличные", "USD", "US$"),
)

private val previewCategories = listOf(
    CategoryGroup(
        CategoryItem("food", "Еда"),
        listOf(CategoryItem("shop", "Продукты"), CategoryItem("cafe", "Кафе")),
    ),
    CategoryGroup(CategoryItem("home", "Жильё"), emptyList()),
    CategoryGroup(CategoryItem("car", "Транспорт"), emptyList()),
)

private val today = LocalDate(2026, 11, 8)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ExpensePreview() {
    CoShellTheme {
        TransactionEditScreen(
            TransactionEditUiState(
                amount = "12 500",
                accounts = previewAccounts,
                accountId = "card",
                categories = previewCategories,
                categoryId = "cafe",
                openFolderId = "food",
                date = today,
                today = today,
            ),
            TransactionEditActions(),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TransferPreview() {
    CoShellTheme {
        TransactionEditScreen(
            TransactionEditUiState(
                isNew = false,
                type = TransactionType.TRANSFER,
                amount = "100",
                toAmount = "",
                accounts = previewAccounts,
                accountId = "cash",
                toAccountId = "card",
                toAmountError = TransactionEditUiState.AmountError.REQUIRED,
                date = LocalDate(2026, 11, 2),
                today = today,
            ),
            TransactionEditActions(),
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun ReceiptInOtherCurrencyPreview() {
    CoShellTheme {
        TransactionEditScreen(
            TransactionEditUiState(
                amount = "14,29",
                accounts = previewAccounts,
                accountId = "cash",
                receiptEnabled = true,
                receiptCurrency = "ARS",
                receiptAmount = "15 000",
                receiptCurrencies = listOf("ARS", "USD", "EUR"),
                categories = previewCategories,
                categoryId = "home",
                date = today,
                today = today,
            ),
            TransactionEditActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoAccountsPreview() {
    CoShellTheme { TransactionEditScreen(TransactionEditUiState(), TransactionEditActions()) }
}
