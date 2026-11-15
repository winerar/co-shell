package app.coshell.reports.category

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.reports.ReportsUiState.Bar
import app.coshell.reports.ReportsUiState.Mark
import app.coshell.reports.ReportsUiState.MonthColumn
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus

/** Numbers of boards `RepDrill` («Дом», November, USD) and `Tr238DrillA` («Кафе»): previews and screenshot tests. */
object ReportCategorySamples {
    private const val TEAL = 0xFF00BCD4
    private const val ORANGE = 0xFFFF9800

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

    /** Board `Tr238DrillA`: «Кафе» in November — +45 % against the usual by the 11th, a year of months, averages 62 and 65. */
    val trend = ReportCategoryUiState(
        name = "Кафе",
        period = ReportPeriod.month(YearMonth(2026, 11)),
        currency = "USD",
        total = "89",
        percent = 12,
        mark = Mark(up = true, percent = 45, expected = "61", byDay = 11),
        trend = listOf(70, 64, 58, 66, 72, 60, 68, 75, 58, 62, 66, 89).let { values ->
            Trend(
                columns = values.mapIndexed { i, v ->
                    MonthColumn(YearMonth(2025, 12).plus(i, DateTimeUnit.MONTH), if (i == values.lastIndex) "$v" else "", v / 89f)
                },
                selected = values.lastIndex,
                average = 65 / 89f,
                recent = "62",
                recentMonths = 3,
                long = "65",
                longMonths = 12,
            )
        },
        days = listOf(
            Day(
                LocalDate(2026, 11, 10),
                RelativeDay.OTHER,
                listOf(Row("c1", Kind.EXPENSE, "Кафе", "Galicia", "Café Martínez", "−38 000 ARS", icon = "cafe", color = ORANGE, author = "Аня")),
                total = "−38 000 ARS",
                totalTone = Kind.EXPENSE,
            ),
            Day(
                LocalDate(2026, 11, 8),
                RelativeDay.OTHER,
                listOf(Row("c2", Kind.EXPENSE, "Кафе", "USDT", "Starbucks", "−24 500 ARS", icon = "cafe", color = ORANGE, author = "Миша")),
                total = "−24 500 ARS",
                totalTone = Kind.EXPENSE,
            ),
        ),
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReportCategoryPreview() = CoShellTheme(darkTheme = true) { ReportCategoryScreen(ReportCategorySamples.state, onBack = {}, onTransactionClick = {}) }

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReportCategoryTrendPreview() = CoShellTheme(darkTheme = true) { ReportCategoryScreen(ReportCategorySamples.trend, onBack = {}, onTransactionClick = {}) }
