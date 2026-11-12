package app.coshell.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.budgets.list.BudgetsUiState
import app.coshell.core.data.sync.SyncIssueKind
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.home.budget.HomeBudget
import app.coshell.home.sync.SyncBannerState
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

internal val homePreviewSample = HomeUiState(
    totals = listOf("1 310,50 USD", "1 900 225,00 ARS"),
    rateDate = LocalDate(2026, 11, 9),
    byCurrency = listOf("105 800,50 RUB", "150,00 USD"),
    spending = HomeUiState.MonthSpending(
        month = LocalDate(2026, 11, 1),
        totals = listOf("412,30\u00A0USD", "598 000,00\u00A0ARS"),
        missing = emptyList(),
        daily = listOf(1200L, 0, 5400, 800, 3000, 0, 0, 9900, 2500) + List(21) { 0L },
        today = 8,
    ),
    income = HomeUiState.MonthTotals(listOf("2 000,00 USD", "2 900 000,00 ARS")),
    attention = HomeUiState.Attention(uncategorized = 14),
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

/** Board `BudHome`: 540 of 1 180 USD left, «Кафе» over, «Дом» ahead of pace. */
internal val homeBudgetPreviewSample = HomeBudget(
    month = YearMonth(2026, 11),
    summary = BudgetsUiState.Summary(
        left = "540\u00A0USD", over = false, spent = "640", planned = "1 180\u00A0USD", fraction = 640f / 1180, pace = 0.37f,
        tone = BulletTone.WARNING, daysLeft = 19, forecast = null,
    ),
    hot = listOf(
        BudgetsUiState.Envelope(
            "cafe", "Кафе", "local_cafe", 0xFFFF7043, null, "95 000", "80 000\u00A0ARS", 95f / 80, 0.37f, BulletTone.DANGER,
            BudgetsUiState.Caption.Over("15 000\u00A0ARS"),
        ),
        BudgetsUiState.Envelope(
            "home", "Дом", "home", 0xFF26C6DA, 3, "310 000", "400 000\u00A0ARS", 310f / 400, 0.37f, BulletTone.WARNING,
            BudgetsUiState.Caption.Ahead("90 000\u00A0ARS", 19),
        ),
    ),
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeBudgetPreview() {
    CoShellTheme(darkTheme = true) { HomeScreen(homePreviewSample, budget = homeBudgetPreviewSample) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    CoShellTheme(darkTheme = false) { HomeScreen(homePreviewSample) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeScreenDarkPreview() {
    val offline = SyncBannerState(SyncIssueKind.OFFLINE, pending = 3, lastSync = "9:12")
    CoShellTheme(darkTheme = true) { HomeScreen(homePreviewSample.copy(attention = HomeUiState.Attention()), sync = offline) }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    CoShellTheme { HomeScreen(HomeUiState()) }
}
