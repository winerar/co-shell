package app.coshell.budgets.detail

import app.coshell.budgets.detail.BudgetDetailUiState.Inner
import app.coshell.budgets.list.BudgetsUiState.Caption
import app.coshell.core.designsystem.components.BulletTone
import app.coshell.core.domain.budgets.BudgetCalculator
import app.coshell.core.domain.budgets.BudgetLedger
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.budgetId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Mockup `BudDetail`: «Дом» 400 000 ARS on 11 November, 300 000 spent; «Коммуналка» inside has its own 70 000, «Аренда» and «Связь» do not. */
class BudgetDetailBoardTest {
    private val ars = CurrencyCode("ARS")
    private val tz = TimeZone.UTC
    private val month = YearMonth(2026, 11)
    private val today = LocalDate(2026, 11, 11)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)

    private val categories = listOf(
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("phone", "Связь", CategoryType.EXPENSE, parentId = "home", sortOrder = 3),
        Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home", sortOrder = 1),
        Category("utilities", "Коммуналка", CategoryType.EXPENSE, parentId = "home", sortOrder = 2),
        Category("old", "Старое", CategoryType.EXPENSE, parentId = "home", archived = true),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
    )

    private fun ars(major: Long) = Money.ofMajor(major, currency = ars)

    private fun limit(categoryId: String, major: Long) = Budget(budgetId(categoryId, month), categoryId, month, ars(major), note = "Аренда до 5-го".takeIf { categoryId == "home" })

    private var nextId = 0

    private fun spend(categoryId: String, major: Long, day: Int) = Transaction(
        id = "t${nextId++}",
        type = TransactionType.EXPENSE,
        accountId = "acc",
        amount = ars(major),
        date = LocalDateTime(2026, 11, day, 12, 0).toInstant(tz),
        categoryId = categoryId,
    )

    private val book = RateBook(emptyList())
    private val ledger = BudgetLedger(listOf(spend("rent", 280_000, 3), spend("utilities", 20_000, 4), spend("cafe", 5_000, 5)), book, tz)
    private val calculator = BudgetCalculator(listOf(limit("home", 400_000), limit("utilities", 70_000), limit("cafe", 80_000)), categories, ledger, book)

    private fun detail(categoryId: String, on: LocalDate = today) =
        buildEnvelopeDetail(categoryId, calculator.month(month, on), categories, ledger, on, format)

    @Test
    fun `the head says what is left, the days and where spending should be by today`() {
        val head = detail("home").first!!
        assertEquals(format.format(ars(100_000)), head.left)
        assertEquals(false, head.over)
        assertEquals(19, head.daysLeft)
        // 400 000 × 11 / 30.
        assertEquals(format.formatNumber(14_666_667, 2), head.paceToday)
        assertEquals(BulletTone.WARNING, head.envelope.tone)
        assertEquals(3, head.envelope.folderSize)
    }

    @Test
    fun `categories of the folder keep their order, with or without their own limit, archived ones only when used`() {
        val inner = detail("home").second
        assertEquals(listOf("rent", "utilities", "phone"), inner.map { it.categoryId })
        assertEquals(Inner.Plain("rent", "Аренда", null, null, format.format(ars(280_000))), inner[0])
        assertEquals(format.format(ars(50_000)), ((inner[1] as Inner.Limited).envelope.caption as Caption.Left).left)
        assertNull((inner[2] as Inner.Plain).spent)
    }

    @Test
    fun `a plain category has no inner rows, a past month no days or pace`() {
        val later = LocalDate(2026, 12, 2)
        val (head, inner) = detail("cafe", later)
        assertEquals(emptyList<Inner>(), inner)
        assertNull(head!!.daysLeft)
        assertNull(head.paceToday)
    }

    @Test
    fun `no limit in the month — no head`() {
        assertEquals(null to emptyList<Inner>(), detail("rent"))
    }
}
