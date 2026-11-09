package app.coshell.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.LocalDate

internal val homePreviewSample = HomeUiState(
    totals = listOf("1 310,50 USD", "1 900 225,00 ARS"),
    rateDate = LocalDate(2026, 11, 9),
    byCurrency = listOf("105 800,50 RUB", "150,00 USD"),
    spending = HomeUiState.MonthSpending(
        month = LocalDate(2026, 11, 1),
        totals = listOf("412,30 USD", "598 000,00 ARS"),
        missing = emptyList(),
        daily = listOf(1200L, 0, 5400, 800, 3000, 0, 0, 9900, 2500) + List(21) { 0L },
        today = 8,
    ),
    income = HomeUiState.MonthTotals(listOf("2 000,00 USD", "2 900 000,00 ARS")),
    attention = HomeUiState.Attention(missingRates = listOf("RUB"), uncategorized = 2),
    accounts = listOf(
        HomeUiState.AccountRow("1", "Карта", "120 800,50 RUB", isNegative = false),
        HomeUiState.AccountRow("2", "Кредитка", "-15 000,00 RUB", isNegative = true, excludedFromTotal = true),
        HomeUiState.AccountRow("3", "Наличные", "150,00 USD", isNegative = false),
    ),
    recent = listOf(
        Row("t1", Kind.EXPENSE, "Кафе", "Карта", "кофе", "-4 500,00 ARS", icon = "local_cafe", color = 0xFFFF7043),
        Row("t2", Kind.INCOME, "Зарплата", "Карта", "", "+2 000,00 USD"),
        Row("t3", Kind.TRANSFER, null, "Карта → Наличные", "", "100,00 USD"),
    ),
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    CoShellTheme(darkTheme = false) { HomeScreen(homePreviewSample) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenDarkPreview() {
    CoShellTheme(darkTheme = true) { HomeScreen(homePreviewSample.copy(attention = HomeUiState.Attention())) }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    CoShellTheme { HomeScreen(HomeUiState()) }
}
