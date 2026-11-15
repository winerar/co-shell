package app.coshell.reports.category

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Board `RepDrill`: «Дом за ноябрь · 290 · 38% трат» — «Аренда» 262 and «Коммуналка» 28, two operations on two days. */
class ReportCategoryBoardTest {
    private val ars = CurrencyCode("ARS")
    private val tz = TimeZone.UTC
    private val today = LocalDate(2026, 11, 11)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)
    private val november = ReportPeriod.month(YearMonth(2026, 11))

    private val categories = listOf(
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
        Category("utilities", "Коммуналка", CategoryType.EXPENSE, parentId = "home"),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("taxi", "Такси", CategoryType.EXPENSE),
    )

    private fun ars(major: Long) = Money.ofMajor(major, currency = ars)

    private fun day(month: Int, d: Int, category: String, major: Long, type: TransactionType = TransactionType.EXPENSE) =
        ReportDay(LocalDate(2026, month, d), category, type, ars(major))

    private var nextId = 0

    private fun operation(category: String?, major: Long, day: Int, type: TransactionType = TransactionType.EXPENSE) = Transaction(
        id = "t${nextId++}",
        type = type,
        accountId = "cash",
        amount = ars(major),
        date = LocalDateTime(2026, 11, day, 12, 0).toInstant(tz),
        categoryId = category,
        toAccountId = if (type == TransactionType.TRANSFER) "card" else null,
        toAmount = if (type == TransactionType.TRANSFER) ars(major) else null,
    )

    private fun input(days: List<ReportDay>, operations: List<Transaction>, compare: Boolean = true, categoryId: String = "home") = DrillInput(
        categoryId = categoryId,
        ledger = ReportLedger(days, categories, RateBook(emptyList()), ars),
        categories = categories,
        period = november,
        startDay = 1,
        compare = compare,
        operations = operations,
        accounts = listOf(Account("cash", "Наличные", ars), Account("card", "Galicia", ars)),
        authors = emptyMap(),
        timeZone = tz,
        today = today,
    )

    private val days = listOf(
        day(11, 5, "rent", 262),
        day(11, 3, "utilities", 28),
        day(11, 4, "cafe", 400),
        day(11, 6, "taxi", 70),
        day(10, 5, "rent", 250),
    )

    @Test
    fun `head, subcategories with shares and grey marks, operations of the category by day`() {
        val operations = listOf(
            operation("rent", 262, 5),
            operation("utilities", 28, 3),
            operation("cafe", 400, 4),
            operation(null, 50, 4, TransactionType.TRANSFER),
        )

        val state = buildCategoryDrill(input(days, operations), format, format)

        assertEquals("Дом", state.name)
        assertEquals("290", state.total)
        assertEquals(38, state.percent)
        assertEquals(listOf("rent" to 90, "utilities" to 10), state.parts.map { it.categoryId to it.percent })
        assertEquals(listOf("262", "28"), state.parts.map { it.value })
        assertEquals(1f, state.parts[0].fraction)
        assertEquals("250", state.parts[0].ghostValue)
        assertEquals("0", state.parts[1].ghostValue)
        assertEquals(listOf(LocalDate(2026, 11, 5), LocalDate(2026, 11, 3)), state.days.map { it.date })
        assertEquals(2, state.operationCount)
        assertEquals(listOf("Аренда", "Коммуналка"), state.days.flatMap { d -> d.rows.map { it.title } })
    }

    @Test
    fun `without comparison the bars have no grey marks`() {
        val state = buildCategoryDrill(input(days, emptyList(), compare = false), format, format)

        assertEquals(listOf(null, null), state.parts.map { it.ghost })
        assertEquals(0, state.operationCount)
    }

    @Test
    fun `a plain category has no bars, one with more refunds than spending has no total but keeps its operations`() {
        val plain = buildCategoryDrill(input(days, emptyList(), categoryId = "cafe"), format, format)
        assertEquals("400", plain.total)
        assertEquals(emptyList<Any>(), plain.parts)

        val refunds = listOf(day(11, 2, "taxi", 100, TransactionType.INCOME))
        val state = buildCategoryDrill(
            input(days + refunds, listOf(operation("taxi", 70, 6), operation("taxi", 100, 2, TransactionType.INCOME)), categoryId = "taxi"),
            format,
            format,
        )
        assertNull(state.total)
        assertEquals(2, state.operationCount)
    }
}
