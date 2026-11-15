package app.coshell.reports.category

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.reports.ReportsUiState.Bar
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Numbers of board `RepDrill` («Дом», November, USD): previews and screenshot tests. */
object ReportCategorySamples {
    private const val TEAL = 0xFF00BCD4

    val state = ReportCategoryUiState(
        name = "Дом",
        period = ReportPeriod.month(YearMonth(2026, 11)),
        currency = "USD",
        total = "290",
        percent = 38,
        parts = listOf(
            Bar("rent", "Аренда", "home", TEAL, "262", 90, 1f, 250 / 262f, "250"),
            Bar("utilities", "Коммуналка", "bolt", TEAL, "28", 10, 28 / 262f, 50 / 262f, "50"),
        ),
        days = listOf(
            Day(
                LocalDate(2026, 11, 5),
                RelativeDay.OTHER,
                listOf(Row("t1", Kind.EXPENSE, "Аренда", "Наличные", "", "−280 000 ARS", icon = "home", color = TEAL, author = "Миша")),
                total = "−280 000 ARS",
                totalTone = Kind.EXPENSE,
            ),
            Day(
                LocalDate(2026, 11, 3),
                RelativeDay.OTHER,
                listOf(Row("t2", Kind.EXPENSE, "Коммуналка", "Galicia", "Свет", "−30 000 ARS", icon = "bolt", color = TEAL, author = "Аня")),
                total = "−30 000 ARS",
                totalTone = Kind.EXPENSE,
            ),
        ),
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReportCategoryPreview() = CoShellTheme(darkTheme = true) { ReportCategoryScreen(ReportCategorySamples.state, onBack = {}, onTransactionClick = {}) }
