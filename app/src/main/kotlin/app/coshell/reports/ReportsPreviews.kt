package app.coshell.reports

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.ReportPeriodKind
import app.coshell.reports.ReportsUiState.Bar
import app.coshell.reports.ReportsUiState.CategoryRow
import app.coshell.reports.ReportsUiState.Mark
import app.coshell.reports.ReportsUiState.Comparison
import app.coshell.reports.ReportsUiState.Capital
import app.coshell.reports.ReportsUiState.CapitalPoint
import app.coshell.reports.ReportsUiState.Income
import app.coshell.reports.ReportsUiState.MemberRow
import app.coshell.reports.ReportsUiState.TopFolder
import app.coshell.reports.ReportsUiState.WhyRow
import app.coshell.reports.ReportsUiState.IncomeColumn
import app.coshell.reports.ReportsUiState.MonthColumn
import app.coshell.reports.ReportsUiState.Months
import app.coshell.reports.ReportsUiState.Other
import app.coshell.reports.ReportsUiState.Spending
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** Numbers of board `RepA` (November, USD): previews, screenshot and smoke tests. */
object ReportsSamples {
    private data class Item(val id: String, val name: String, val icon: String, val color: Long, val amount: Int, val percent: Int, val previous: Int)

    private val items = listOf(
        Item("home", "Дом", "home", 0xFF00BCD4, 290, 38, 300),
        Item("food", "Продукты", "food", 0xFF8BC34A, 121, 16, 150),
        Item("education", "Образование", "education", 0xFF9C27B0, 120, 16, 130),
        Item("cafe", "Кафе", "cafe", 0xFFFF9800, 89, 12, 105),
        Item("pharmacy", "Аптека", "pharmacy", 0xFFE91E63, 70, 9, 30),
        Item("subscriptions", "Подписки", "subscriptions", 0xFF673AB7, 21, 3, 22),
        Item("taxi", "Такси", "taxi", 0xFFFFEB3B, 14, 2, 20),
        Item("transport", "Транспорт", "transport", 0xFFFFC107, 8, 1, 15),
    )
    private const val MAX = 300f

    /** Board `Tr238MarkA` / `Tr238All`: noticeably more or less than by the 11th of the three months before. */
    /** Bars are folders: only these of [marks] show on «На что ушли деньги». */
    private val barMarks = setOf("cafe", "food")

    private val marks = mapOf(
        "cafe" to Mark(up = true, percent = 45, expected = "61", byDay = 11),
        "food" to Mark(up = false, percent = 33, expected = "180", byDay = 11),
        "subscriptions" to Mark(up = true, percent = 50, expected = "14", byDay = 11),
        "taxi" to Mark(up = false, percent = 30, expected = "20", byDay = 11),
    )

    val spending = Spending(
        total = "760",
        comparison = Comparison.SoFar(days = 11, kind = ReportPeriodKind.MONTH, previousDay = 11, previous = "820", faster = false),
        bars = items.take(5).map { Bar(it.id, it.name, it.icon, it.color, it.amount.toString(), it.percent, it.amount / MAX, it.previous / MAX, it.previous.toString(), marks[it.id].takeIf { m -> it.id in barMarks }) },
        other = Other("Транспорт", 3, "70", 9),
        categories = items.map {
            CategoryRow(it.id, it.name, it.icon, it.color, it.amount.toString(), it.percent, it.amount / MAX, it.previous / MAX, it.previous.toString(), (it.amount - it.previous) * 100L, it.amount * 100L, null, marks[it.id])
        },
    )

    val months = Months(
        columns = listOf(1_180, 1_320, 1_090, 1_410, 1_350, 760).mapIndexed { i, v ->
            MonthColumn(YearMonth(2026, 6 + i), "%,d".format(java.util.Locale.ROOT, v).replace(',', ' '), v / 1_410f)
        },
        selected = 5,
    )

    /** Board `Rp189IeA`: income next to spending, June … November (November so far). */
    val income = Income(
        columns = listOf(1_650 to 1_180, 1_600 to 1_320, 1_700 to 1_090, 1_300 to 1_410, 1_620 to 1_350, 1_580 to 760).mapIndexed { i, (e, s) ->
            IncomeColumn(YearMonth(2026, 6 + i), "%,d".format(java.util.Locale.ROOT, e).replace(',', ' '), "%,d".format(java.util.Locale.ROOT, s).replace(',', ' '), e / 1_700f, s / 1_700f)
        },
        selected = 5,
        earned = "1 580",
        spent = "760",
        saved = "+820",
        overspent = false,
        rate = 51,
        running = true,
        averageRate = 19,
        averageMonths = 5,
    )

    /** Board `Rp189WhoA`: spending by who added it. */
    val members = listOf(
        MemberRow("misha", "Миша", "430", 57, 1f, listOf(TopFolder("Дом", "262"), TopFolder("Продукты", "70"), TopFolder("Кафе", "38"))),
        MemberRow(null, null, "330", 43, 330 / 430f, listOf(TopFolder("Образование", "120"), TopFolder("Аптека", "70"), TopFolder("Продукты", "51"))),
    )

    /** Board `Rp189NwB`: a year of month ends (USD), why the last three months changed. */
    val capital = Capital(
        value = "8 503",
        change = "+2 403",
        falling = false,
        percent = 39,
        months = 12,
        points = listOf(6_100, 6_150, 6_230, 6_090, 6_200, 6_280, 6_750, 6_990, 7_580, 7_440, 7_700, 8_503).mapIndexed { i, v ->
            val month = YearMonth(2025, 12).let { if (i == 0) it else YearMonth(2026, i) }
            CapitalPoint(month, "%,d".format(java.util.Locale.ROOT, v).replace(',', ' '), v * 100L)
        },
        why = listOf(
            WhyRow(YearMonth(2026, 11), running = true, saved = "+820", savedNegative = false, rates = "-17", total = "+803", totalNegative = false),
            WhyRow(YearMonth(2026, 10), running = false, saved = "+270", savedNegative = false, rates = "-10", total = "+260", totalNegative = false),
            WhyRow(YearMonth(2026, 9), running = false, saved = "-110", savedNegative = true, rates = "-30", total = "-140", totalNegative = true),
        ),
    )

    val state = ReportsUiState(
        period = ReportPeriod.month(YearMonth(2026, 11)),
        today = LocalDate(2026, 11, 11),
        currency = "USD",
        currencies = listOf("USD", "ARS", "AMD", "RUB"),
        spending = spending,
        months = months,
        income = income,
        members = members,
        capital = capital,
    )

    val empty = state.copy(spending = null)
}

@Preview
@Composable
private fun ReportsPreview() = CoShellTheme(darkTheme = true) { ReportsScreen(ReportsSamples.state, ReportsActions()) }

@Preview
@Composable
private fun ReportsEmptyPreview() = CoShellTheme { ReportsScreen(ReportsSamples.empty, ReportsActions()) }
