package app.coshell.budgets.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.budgets.edit.BudgetEditUiState.Choice
import app.coshell.budgets.edit.BudgetEditUiState.Inside
import app.coshell.core.designsystem.theme.CoShellTheme
import kotlinx.datetime.YearMonth

// Mockup `BudEdit`: a limit on folder «Дом».
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun BudgetEditPreview() {
    CoShellTheme {
        BudgetEditScreen(
            BudgetEditUiState(
                month = YearMonth(2026, 11),
                folders = listOf(Choice("home", "Дом", "home", 0xFF00BCD4)),
                categoryId = "home",
                amount = "400 000",
                currency = "ARS",
                currencies = listOf("USD", "ARS"),
                average = "372 000 ARS",
                inside = listOf(
                    Inside("rent", "Аренда", "home", 0xFF00BCD4, null),
                    Inside("utilities", "Коммуналка", "bolt", 0xFF00BCD4, "70 000 ARS"),
                    Inside("phone", "Связь", "mobile", 0xFF00BCD4, null),
                ),
                note = "Аренда 280 000 до 5-го, свет и газ ~60 000",
                hasLimit = true,
            ),
            BudgetEditActions(),
        )
    }
}
