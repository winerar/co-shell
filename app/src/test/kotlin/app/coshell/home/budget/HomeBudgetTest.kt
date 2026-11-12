package app.coshell.home.budget

import app.coshell.budgets.BudgetSource
import app.coshell.budgets.list.BudgetsUiState.Caption
import app.coshell.budgets.list.BudgetsUiState.Envelope
import app.coshell.core.designsystem.components.BulletTone
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import app.coshell.core.model.budgetId
import app.coshell.core.domain.reports.ReportDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** «Обзор» on 11 November 2026 (19 days left): «Кафе» over, «Дом» ahead of pace, «Продукты» in plan. */
class HomeBudgetTest {
    private val ars = CurrencyCode("ARS")
    private val month = YearMonth(2026, 11)
    private val today = LocalDate(2026, 11, 11)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)

    private val categories = listOf(
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("groceries", "Продукты", CategoryType.EXPENSE),
    )

    private fun ars(major: Long) = Money.ofMajor(major, currency = ars)

    private fun limit(categoryId: String, major: Long) = Budget(budgetId(categoryId, month), categoryId, month, ars(major))

    private fun day(categoryId: String, major: Long, day: Int) = ReportDay(LocalDate(2026, 11, day), categoryId, TransactionType.EXPENSE, ars(major))

    private fun snapshot(budgets: List<Budget>) = BudgetSource.Snapshot(
        budgets = budgets,
        categories = categories,
        days = listOf(day("rent", 300_000, 3), day("cafe", 95_000, 8), day("groceries", 40_000, 9)),
        rates = emptyList(),
        mainCurrency = ars,
        startDay = 1,
    )

    @Test
    fun `no limits - no tile and no block`() {
        assertNull(buildHomeBudget(snapshot(emptyList()), today, format))
    }

    @Test
    fun `the tile has the month left and the block the limits that need a look, over first`() {
        val budget = buildHomeBudget(snapshot(listOf(limit("home", 400_000), limit("cafe", 80_000), limit("groceries", 350_000))), today, format)!!
        assertEquals(month, budget.month)
        assertEquals(19, budget.summary.daysLeft)
        assertEquals(format.format(ars(395_000)), budget.summary.left)
        assertEquals(listOf("cafe", "home"), budget.hot.map { it.categoryId })
        assertEquals(Caption.Over(format.format(ars(15_000))), budget.hot[0].caption)
    }

    @Test
    fun `everything in plan - the tile without the block`() {
        val budget = buildHomeBudget(snapshot(listOf(limit("groceries", 350_000))), today, format)!!
        assertEquals(BulletTone.NORMAL, budget.summary.tone)
        assertEquals(emptyList<Envelope>(), budget.hot)
    }

    @Test
    fun `an inner limit that burns is a row of its own, and only three rows`() {
        val calmFolder = envelope("food", BulletTone.NORMAL, children = listOf(envelope("sweets", BulletTone.DANGER)))
        val list = listOf(envelope("a", BulletTone.DANGER), envelope("b", BulletTone.WARNING), envelope("c", BulletTone.WARNING), calmFolder)
        assertEquals(listOf("a", "sweets", "b"), hotEnvelopes(list).map { it.categoryId })
        val hotFolder = envelope("home", BulletTone.WARNING, children = listOf(envelope("rent", BulletTone.NORMAL)))
        assertEquals(listOf(emptyList<Envelope>()), hotEnvelopes(listOf(hotFolder)).map { it.children })
    }

    private fun envelope(id: String, tone: BulletTone, children: List<Envelope> = emptyList()) =
        Envelope(id, id, null, null, null, "1", "2 ARS", 0.5f, null, tone, Caption.Idle, children = children)
}
