package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CategoryTrendTest {
    private val month = budgetPeriod(november)
    private val eleventh = LocalDate(2026, 11, 11)
    private val over = LocalDate(2026, 12, 1)

    /** History starts in August: every month rent 1 000, cafe 300, taxi 50 (usual total 1 350). */
    private val usual = (8..10).flatMap { m ->
        listOf(day(m, 2, "rent", ars(1_000)), day(m, 3, "cafe", ars(300)), day(m, 4, "taxi", ars(50)))
    }

    private fun trend(id: String, today: LocalDate, vararg november: ReportDay) = categoryTrend(
        ledger(*(usual + november).toTypedArray()), trendCategoryIds(id, categories), month, startDay = 1, today = today,
    )

    @Test
    fun `a folder sums its subcategories, a category is itself`() {
        assertEquals(setOf("home", "rent", "utilities"), trendCategoryIds("home", categories))
        assertEquals(setOf("cafe"), trendCategoryIds("cafe", categories))
    }

    @Test
    fun `twelve months up to the chosen one, nothing before the history, averages over the full months`() {
        val trend = trend("home", eleventh, day(11, 2, "rent", ars(1_000)), day(11, 5, "utilities", ars(200)))

        assertEquals(12, trend.months.size)
        assertEquals(budgetPeriod(kotlinx.datetime.YearMonth(2025, 12)), trend.months.first().period)
        assertEquals(month, trend.months.last().period)
        assertEquals(List<Money?>(8) { null } + listOf(ars(1_000), ars(1_000), ars(1_000), ars(1_200)), trend.months.map { it.amount })
        assertEquals(ars(1_000), trend.average3)
        assertEquals(ars(1_000), trend.average12)
    }

    @Test
    fun `the long average counts a quiet month inside the history`() {
        val withMay = categoryTrend(
            ledger(day(5, 10, "cafe", ars(900)), *usual.toTypedArray()),
            setOf("cafe"), month, startDay = 1, today = eleventh,
        )

        // May 900, June and July 0, August–October 300 each: 1 800 over six months.
        assertEquals(ars(300), withMay.average12)
        assertEquals(ars(300), withMay.average3)
        assertEquals(ars(0), withMay.months[6].amount)
        assertNull(withMay.months[4].amount)
    }

    @Test
    fun `a running month is compared with the same days of the months before`() {
        val mark = trend("cafe", eleventh, day(11, 3, "cafe", ars(600))).mark

        assertEquals(UsualMark(UsualDirection.ABOVE, ars(600), ars(300), 100), mark)
        assertEquals(ars(300), mark?.difference)
    }

    @Test
    fun `rent paid early in the month is usual, not above`() {
        assertNull(trend("home", eleventh, day(11, 2, "rent", ars(1_000))).mark)
    }

    @Test
    fun `spending earlier than usual is not above`() {
        val lateCafe = (8..10).flatMap { m -> listOf(day(m, 2, "rent", ars(1_000)), day(m, 20, "cafe", ars(300))) }
        val trend = categoryTrend(
            ledger(*(lateCafe + day(11, 3, "cafe", ars(300))).toTypedArray()), setOf("cafe"), month, 1, eleventh,
        )

        assertNull(trend.mark)
    }

    @Test
    fun `no mark in the first days of a month`() {
        assertNull(trend("cafe", LocalDate(2026, 11, 5), day(11, 3, "cafe", ars(900))).mark)
    }

    @Test
    fun `small differences are not marked, by amount or by percent`() {
        // Cafe: 330 against 300 — below 5 % of the usual total by the 11th (67,50).
        assertNull(trend("cafe", eleventh, day(11, 3, "cafe", ars(330))).mark)
        // Home: 1 150 against 1 000 — enough by amount, but only +15 %.
        assertNull(trend("home", eleventh, day(11, 2, "rent", ars(1_150))).mark)
    }

    @Test
    fun `a month that is over compares whole amounts, below usual too`() {
        val mark = trend("cafe", over, day(11, 2, "rent", ars(1_000))).mark

        assertEquals(UsualMark(UsualDirection.BELOW, ars(0), ars(300), -100), mark)
    }

    @Test
    fun `more refunds than spending count as zero`() {
        val trend = trend("cafe", over, day(11, 3, "cafe", ars(500), TransactionType.INCOME))

        assertEquals(ars(0), trend.months.last().amount)
        assertEquals(UsualDirection.BELOW, trend.mark?.direction)
    }

    @Test
    fun `nothing to compare with before the history`() {
        val trend = categoryTrend(ledger(day(11, 3, "cafe", ars(300))), setOf("cafe"), month, 1, over)

        assertNull(trend.average3)
        assertNull(trend.average12)
        assertNull(trend.mark)
        assertEquals(ars(300), trend.months.last().amount)
    }

    @Test
    fun `marks of the folders - only the noticeable ones, a new one without a percent`() {
        val marks = usualMarks(
            ledger(
                *usual.toTypedArray(),
                day(11, 2, "rent", ars(1_000)), day(11, 3, "cafe", ars(600)), day(11, 4, "taxi", ars(50)),
                day(11, 5, "gift", ars(100)),
            ),
            categories, month, startDay = 1, today = over,
        )

        assertEquals(
            mapOf(
                "cafe" to UsualMark(UsualDirection.ABOVE, ars(600), ars(300), 100),
                "gift" to UsualMark(UsualDirection.ABOVE, ars(100), ars(0), null),
            ),
            marks,
        )
    }

    @Test
    fun `no marks without history`() {
        assertEquals(emptyMap(), usualMarks(ledger(day(11, 3, "cafe", ars(300))), categories, month, 1, over))
    }
}
