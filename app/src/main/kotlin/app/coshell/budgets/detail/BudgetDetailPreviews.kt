package app.coshell.budgets.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.budgets.detail.BudgetDetailUiState.Inner
import app.coshell.budgets.list.BudgetsUiState.Caption
import app.coshell.budgets.list.BudgetsUiState.Envelope
import app.coshell.core.designsystem.components.BulletTone
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.transactions.list.TransactionsUiState
import kotlinx.datetime.YearMonth

// Numbers of mockup `BudDetail`.
private val home = Envelope(
    "home", "Дом", "home", 0xFF00BCD4, 3, "310 000", "400 000 ARS", 0.78f, 0.37f, BulletTone.WARNING, Caption.Ahead("90 000 ARS", 19),
)

private val sample = BudgetDetailUiState(
    month = YearMonth(2026, 11),
    name = "Дом",
    head = BudgetDetailUiState.Head(home, left = "90 000 ARS", over = false, daysLeft = 19, paceToday = "147 000"),
    note = "Аренда 280 000 до 5-го, свет и газ ~60 000",
    categories = listOf(
        Inner.Plain("rent", "Аренда", "home", 0xFF00BCD4, "280 000 ARS"),
        Inner.Limited(
            Envelope("utilities", "Коммуналка", "bolt", 0xFF00BCD4, null, "30 000", "70 000 ARS", 0.43f, 0.37f, BulletTone.NORMAL, Caption.Left("40 000 ARS", null)),
        ),
        Inner.Plain("phone", "Связь", "mobile", 0xFF00BCD4, null),
    ),
    operations = listOf(
        TransactionsUiState.Row("t1", TransactionsUiState.Kind.EXPENSE, "Аренда", "Наличные", "", "−280 000 ARS", icon = "home", color = 0xFF00BCD4, author = "Миша"),
        TransactionsUiState.Row("t2", TransactionsUiState.Kind.EXPENSE, "Коммуналка", "Galicia", "Свет", "−30 000 ARS", icon = "bolt", color = 0xFF00BCD4),
    ),
)

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun BudgetDetailPreview() {
    CoShellTheme { BudgetDetailScreen(sample, BudgetDetailActions(), categoryId = "home") }
}

@Preview(showBackground = true)
@Composable
private fun BudgetDetailNoLimitPreview() {
    CoShellTheme { BudgetDetailScreen(BudgetDetailUiState(month = YearMonth(2026, 11), name = "Дом"), BudgetDetailActions(), categoryId = "home") }
}
