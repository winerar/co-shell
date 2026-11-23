package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.october
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.model.TransactionType
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MonthRecapTest {
    private val month = budgetPeriod(october)
    private val before = budgetPeriod(YearMonth(2026, 9))

    private fun recap(vararg days: ReportDay, withHistory: Boolean = true, limits: LimitsTally? = null) =
        monthRecap(ledger(*days), categories, month, before.takeIf { withHistory }, limits)

    /** Records on the days [from]..[to] of October, [amount] each, plus some of September. */
    private fun run(from: Int, to: Int, amount: Long = 10) = (from..to).map { day(10, it, "cafe", ars(amount)) }.toTypedArray()

    @Test
    fun `top categories are ordered with shares in percents`() {
        val recap = recap(day(10, 2, "cafe", ars(300)), day(10, 3, "home", ars(700)))
        assertEquals(listOf("home", "cafe"), recap.top.map { it.categoryId })
        assertEquals(listOf(70, 30), recap.top.map { it.percent })
    }

    @Test
    fun `weeks are seven day slices and the busiest one is found`() {
        val recap = recap(day(10, 2, "cafe", ars(100)), day(10, 9, "cafe", ars(500)), day(10, 31, "cafe", ars(1)))
        assertEquals(5, recap.weeks.size)
        assertEquals(1, recap.busiestWeek)
        assertEquals(ars(1), recap.weeks.last())
    }

    @Test
    fun `weekday and weekend averages count only days with spending`() {
        // 2026-10-03 is a Saturday, 10-05 a Monday, 10-06 a Tuesday.
        val recap = recap(day(10, 3, "cafe", ars(100)), day(10, 5, "cafe", ars(30)), day(10, 6, "cafe", ars(50)))
        assertEquals(ars(100), recap.weekendAverage)
        assertEquals(ars(40), recap.weekdayAverage)
    }

    @Test
    fun `a category with spending now and none before is new, only with history`() {
        val withHistory = recap(day(9, 5, "home", ars(10)), day(10, 5, "home", ars(10)), day(10, 6, "cafe", ars(10)))
        assertEquals(listOf("cafe"), withHistory.newCategoryIds)
        assertTrue(recap(day(10, 6, "cafe", ars(10)), withHistory = false).newCategoryIds.isEmpty())
    }

    @Test
    fun `the streak is the longest run of days with records, income counts as a record`() {
        val recap = recap(*run(1, 3), day(10, 4, "salary", ars(5), TransactionType.INCOME), *run(5, 5), *run(8, 9))
        assertEquals(5, recap.streak)
        assertEquals(7, recap.recordedDays)
    }

    @Test
    fun `cards without facts are left out and the final one stays`() {
        assertEquals(listOf(RecapCard.FINAL), recap(withHistory = false).cards)
        val full = recap(day(9, 5, "home", ars(10)), day(10, 3, "cafe", ars(10)), day(10, 5, "home", ars(10)))
        assertEquals(
            listOf(RecapCard.TOP_CATEGORIES, RecapCard.VS_PREVIOUS, RecapCard.WEEKS, RecapCard.NEW_CATEGORY, RecapCard.WEEKEND, RecapCard.FINAL),
            full.cards,
        )
    }

    @Test
    fun `change against the previous month, no comparison without it`() {
        assertEquals(-50, recap(day(9, 5, "cafe", ars(200)), day(10, 5, "cafe", ars(100))).changePercent)
        assertNull(recap(day(10, 5, "cafe", ars(100)), withHistory = false).changePercent)
    }

    // One test per phrase rule, in the order the rules are tried.

    @Test
    fun `few days with records — a good start, before anything else`() {
        assertEquals(RecapPhrase.GOOD_START, recap(*run(1, 4), day(9, 5, "cafe", ars(1_000))).phrase)
    }

    @Test
    fun `no history — the first month recorded`() {
        assertEquals(RecapPhrase.FIRST_MONTH, recap(*run(1, 6), withHistory = false).phrase)
    }

    @Test
    fun `spent noticeably less — lighter, even when limits were kept`() {
        val limits = LimitsTally(total = 2, within = 2, over = emptyList())
        assertEquals(RecapPhrase.LIGHTER, recap(*run(1, 6), day(9, 5, "cafe", ars(1_000)), limits = limits).phrase)
    }

    @Test
    fun `all limits kept`() {
        val limits = LimitsTally(total = 2, within = 2, over = emptyList())
        assertEquals(RecapPhrase.LIMITS_KEPT, recap(*run(1, 6, 100), day(9, 5, "cafe", ars(500)), limits = limits).phrase)
    }

    @Test
    fun `a week of records in a row`() {
        assertEquals(RecapPhrase.STREAK, recap(*run(1, 7, 100), day(9, 5, "cafe", ars(500))).phrase)
    }

    @Test
    fun `about the same as the month before`() {
        assertEquals(RecapPhrase.SAME_AS_BEFORE, recap(*run(1, 3, 100), *run(5, 7, 100), *run(9, 9, 100), day(9, 5, "cafe", ars(700))).phrase)
    }

    @Test
    fun `more spent but something left of the income`() {
        val recap = recap(*run(1, 3, 100), *run(5, 7, 100), day(10, 20, "salary", ars(5_000), TransactionType.INCOME), day(9, 5, "cafe", ars(300)))
        assertEquals(RecapPhrase.SAVED, recap.phrase)
    }

    @Test
    fun `more spent and nothing left — busy but recorded, without guilt`() {
        assertEquals(RecapPhrase.BUSY_BUT_RECORDED, recap(*run(1, 3, 100), *run(5, 7, 100), day(9, 5, "cafe", ars(300))).phrase)
    }
}
