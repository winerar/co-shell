package app.coshell.budgets.list

import app.coshell.budgets.list.BudgetsUiState.Caption
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.domain.budgets.BudgetCalculator
import app.coshell.core.domain.budgets.BudgetLedger
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.budgetId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * November 2026, the 11th of 30 days: «Дом» (folder, 400 000 ARS) is ahead of pace, «Коммуналка» inside it is fine,
 * «Кафе» is over, «Продукты» (its folder «Еда» has no limit) is in plan, «Подарки» (50 USD) untouched.
 */
class BudgetBoardTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val tz = TimeZone.UTC
    private val month = YearMonth(2026, 11)
    private val today = LocalDate(2026, 11, 11)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)

    private val categories = listOf(
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
        Category("utilities", "Коммуналка", CategoryType.EXPENSE, parentId = "home"),
        Category("food", "Еда", CategoryType.EXPENSE),
        Category("groceries", "Продукты", CategoryType.EXPENSE, parentId = "food"),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("gifts", "Подарки", CategoryType.EXPENSE),
    )

    private fun ars(major: Long) = Money.ofMajor(major, currency = ars)

    private fun limit(categoryId: String, amount: Money) = Budget(budgetId(categoryId, month), categoryId, month, amount)

    private var nextId = 0

    private fun spend(categoryId: String, major: Long, day: Int) = Transaction(
        id = "t${nextId++}",
        type = TransactionType.EXPENSE,
        accountId = "acc",
        amount = ars(major),
        date = LocalDateTime(2026, 11, day, 12, 0).toInstant(tz),
        categoryId = categoryId,
    )

    private val budgets = listOf(
        limit("home", ars(400_000)),
        limit("utilities", ars(70_000)),
        limit("cafe", ars(80_000)),
        limit("groceries", ars(350_000)),
        limit("gifts", Money.ofMajor(50, currency = usd)),
    )
    private val operations = listOf(
        spend("rent", 280_000, 3),
        spend("utilities", 20_000, 4),
        spend("cafe", 95_000, 8),
        spend("groceries", 40_000, 9),
    )
    private val book = RateBook(listOf(ExchangeRate(LocalDate(2026, 11, 1), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED)))
    private val calculator = BudgetCalculator(budgets, categories, BudgetLedger(operations, book, tz), book)

    private fun envelopes() = buildEnvelopes(calculator.month(month, today), categories, format)

    @Test
    fun `over first, then ahead of pace, in plan, untouched, and a limit inside a limited folder goes under it`() {
        val list = envelopes()
        assertEquals(listOf("cafe", "home", "groceries", "gifts"), list.map { it.categoryId })
        assertEquals(listOf(BulletTone.DANGER, BulletTone.WARNING, BulletTone.NORMAL, BulletTone.IDLE), list.map { it.tone })
        assertEquals(listOf("utilities"), list[1].children.map { it.categoryId })
    }

    @Test
    fun `folder shows its size and spending of all its categories`() {
        val home = envelopes().single { it.categoryId == "home" }
        assertEquals(2, home.folderSize)
        assertEquals(format.formatNumber(30_000_000, 2), home.spent)
        assertEquals(format.format(ars(400_000)), home.limit)
        assertEquals(0.75f, home.fraction, 0.001f)
        assertEquals(11f / 30, home.pace!!, 0.001f)
        assertNull(envelopes().single { it.categoryId == "cafe" }.folderSize)
    }

    @Test
    fun `captions follow the state`() {
        val byId = envelopes().flatMap { listOf(it) + it.children }.associateBy { it.categoryId }
        assertEquals(Caption.Over(format.format(ars(15_000))), byId.getValue("cafe").caption)
        assertEquals(Caption.Ahead(format.format(ars(100_000)), 19), byId.getValue("home").caption)
        val groceries = byId.getValue("groceries").caption as Caption.Left
        assertEquals(format.format(ars(310_000)), groceries.left)
        assertNotNull(groceries.perDay)
        assertEquals(Caption.Idle, byId.getValue("gifts").caption)
        assertTrue(byId.getValue("cafe").fraction > 1f)
    }

    @Test
    fun `counts include inner limits, filters keep a folder for its matching limits`() {
        val list = envelopes()
        assertEquals(
            mapOf(BudgetFilter.ALL to 5, BudgetFilter.HOT to 2, BudgetFilter.IN_PLAN to 2, BudgetFilter.IDLE to 1),
            envelopeCounts(list),
        )
        val state = BudgetsUiState(envelopes = list, counts = envelopeCounts(list))
        val hot = state.copy(filter = BudgetFilter.HOT).visible
        assertEquals(listOf("cafe", "home"), hot.map { it.categoryId })
        assertEquals(emptyList<String>(), hot[1].children.map { it.categoryId })
        val inPlan = state.copy(filter = BudgetFilter.IN_PLAN).visible
        assertEquals(listOf("home", "groceries"), inPlan.map { it.categoryId })
        assertEquals(listOf("utilities"), inPlan[0].children.map { it.categoryId })
    }

    @Test
    fun `summary in the main currency counts a folder once and warns about the forecast`() {
        val summary = buildSummary(calculator.summary(month, today, ars), today, format)
        // 400 000 + 80 000 + 350 000 + 50 USD × 1000; «Коммуналка» is a part of «Дом».
        assertEquals(format.format(ars(880_000)), summary.planned)
        assertEquals(format.formatNumber(43_500_000, 2), summary.spent)
        assertEquals(format.format(ars(445_000)), summary.left)
        assertEquals(false, summary.over)
        assertEquals(BulletTone.WARNING, summary.tone)
        assertEquals(19, summary.daysLeft)
        assertNotNull(summary.forecast?.overBy)
    }

    @Test
    fun `a past month has no pace, days or forecast`() {
        val later = LocalDate(2026, 12, 20)
        val summary = buildSummary(calculator.summary(month, later, ars), later, format)
        assertNull(summary.pace)
        assertNull(summary.daysLeft)
        assertNull(summary.forecast)
        val home = buildEnvelopes(calculator.month(month, later), categories, format).single { it.categoryId == "home" }
        assertNull(home.pace)
        assertEquals(Caption.Left(format.format(ars(100_000)), null), home.caption)
    }

    @Test
    fun `spending exactly the limit early in the month is paid, not ahead of pace`() {
        val rent = listOf(limit("rent", ars(280_000)))
        val paid = BudgetCalculator(rent, categories, BudgetLedger(operations, book, tz), book)
        val envelope = buildEnvelopes(paid.month(month, today), categories, format).single()
        assertEquals(Caption.Paid, envelope.caption)
        assertEquals(BulletTone.NORMAL, envelope.tone)
    }

    @Test
    fun `a filter with nothing left falls back to all`() {
        val state = BudgetsUiState(envelopes = envelopes(), filter = BudgetFilter.IDLE)
        assertEquals(listOf("gifts"), state.visible.map { it.categoryId })
        assertTrue(BudgetsUiState(envelopes = envelopes()).visible.size == 4)
    }
}
