package app.coshell.reports

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import app.coshell.reports.ReportsUiState.TopFolder
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MembersBoardTest {
    private val usd = CurrencyCode("USD")
    private val categories = listOf(
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
    )

    private fun day(author: String?, category: String, units: Long) =
        ReportDay(LocalDate(2026, 11, 5), category, TransactionType.EXPENSE, Money(units * 100, usd), author)

    private fun members(others: Map<String, String>, vararg days: ReportDay) = buildMembers(
        ReportInput(
            ReportLedger(days.toList(), categories, RateBook.of(emptyList()), usd),
            categories,
            ReportPeriod.month(YearMonth(2026, 11)),
            1,
            LocalDate(2026, 11, 11),
            compare = true,
        ),
        others,
        MoneyFormat.RU.copy(trimZeroFraction = true),
    )

    @Test
    fun `a row per member on one scale, names from the family, me without a name`() {
        val rows = members(mapOf("anya" to "Аня"), day("me", "rent", 262), day(null, "cafe", 38), day("anya", "cafe", 150), day("gone", "gone", 10))

        assertEquals(listOf(null, "anya"), rows.map { it.memberId })
        assertEquals(listOf(null, "Аня"), rows.map { it.name })
        assertEquals(listOf("310", "150"), rows.map { it.value })
        assertEquals(listOf(67, 33), rows.map { it.percent })
        assertEquals(1f, rows[0].fraction, 0.001f)
        assertEquals(listOf(TopFolder("Дом", "262"), TopFolder("Кафе", "38"), TopFolder(null, "10")), rows[0].top)
    }

    @Test
    fun `one-person family — no card`() {
        assertTrue(members(emptyMap(), day("me", "cafe", 10)).isEmpty())
    }
}
