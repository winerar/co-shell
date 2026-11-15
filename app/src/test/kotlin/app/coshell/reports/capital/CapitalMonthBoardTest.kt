package app.coshell.reports.capital

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.reportMonths
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CapitalMonthBoardTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val format = MoneyFormat(trimZeroFraction = true)

    // USD→ARS 1 000 from 1 Sep, 2 000 from 5 Nov: the peso halves in November.
    private val book = RateBook.of(
        listOf(
            ExchangeRate(LocalDate(2026, 9, 1), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED),
            ExchangeRate(LocalDate(2026, 11, 5), usd, ars, Rate.parse("2000"), RateOrigin.DOWNLOADED),
        ),
    )
    private val cash = Account("cash", "Доллары", usd, type = AccountType.CASH, initialBalanceMinor = 1_000_00)
    private val galicia = Account("galicia", "Galicia", ars, type = AccountType.CARD, initialBalanceMinor = 1_000_000_00)
    private val anyas = Account("ameria", "Ameria", usd, type = AccountType.CARD, initialBalanceMinor = 400_00, isShared = false, ownerMemberId = "anya")
    private val hidden = Account("debt", "Долг", usd, initialBalanceMinor = -500_00, excludedFromTotal = true)

    private fun input(others: Map<String, String> = emptyMap()) = CapitalInput(
        accounts = listOf(cash, galicia, anyas, hidden),
        days = listOf(BalanceDay(LocalDate(2026, 10, 3), "cash", 200_00), BalanceDay(LocalDate(2026, 11, 2), "cash", -100_00)),
        book = book,
        currency = usd,
        periods = reportMonths(YearMonth(2026, 11), 3),
        today = LocalDate(2026, 11, 11),
        others = others,
    )

    private fun build(view: CapitalView, others: Map<String, String> = emptyMap()) = buildCapitalMonth(input(others), view, format, format)

    private fun String.plain() = replace(' ', ' ').replace(' ', ' ').replace('−', '-')

    @Test
    fun `the month picked on the line, its capital and change since the month before`() {
        val state = build(CapitalView(YearMonth(2026, 10)))

        assertEquals(1, state.selected)
        assertEquals(YearMonth(2026, 10), state.month)
        assertEquals(LocalDate(2026, 10, 31), state.asOf)
        assertEquals("2 600", state.value.plain())
        assertEquals("+200", state.change?.plain())
        assertEquals(3, state.count)
        assertEquals(listOf("2 400", "2 600", "2 000"), state.points.map { it.value.plain() })
    }

    @Test
    fun `the first month has no change, a month off the line falls back to the last`() {
        assertNull(build(CapitalView(YearMonth(2026, 9))).change)
        val state = build(CapitalView(YearMonth(2025, 1)))
        assertEquals(2, state.selected)
        assertTrue(state.falling)
    }

    @Test
    fun `grouped by type, the first group open, a flipped one opens, everything open while searching`() {
        val groups = build(CapitalView(YearMonth(2026, 10))).groups
        assertEquals(listOf(listOf("Ameria", "Galicia"), listOf("Доллары")), groups.map { g -> g.rows.map { it.name } })
        assertEquals(listOf(true, false), groups.map { it.expanded })
        assertEquals("1 400 USD", groups[0].total?.plain())

        val flipped = build(CapitalView(YearMonth(2026, 10), flipped = setOf(groups[1].key))).groups
        assertEquals(listOf(true, true), flipped.map { it.expanded })

        val found = build(CapitalView(YearMonth(2026, 10), query = "долл")).groups
        assertEquals(listOf(listOf("Доллары")), found.map { g -> g.rows.map { it.name } })
        assertTrue(found.single().expanded)
    }

    @Test
    fun `by amount — largest first, by change — the biggest move first, the move under the balance`() {
        val byAmount = build(CapitalView(YearMonth(2026, 11), order = CapitalOrder.AMOUNT))
        assertEquals(listOf("Доллары", "Galicia", "Ameria"), byAmount.rows.map { it.name })
        assertTrue(byAmount.groups.isEmpty())
        assertEquals("≈ 500 USD", byAmount.rows[1].converted?.plain())

        // November: the peso lost 500, the dollars −100, Ameria did not move.
        val byChange = build(CapitalView(YearMonth(2026, 11), order = CapitalOrder.CHANGE))
        assertEquals(listOf("Galicia", "Доллары", "Ameria"), byChange.rows.map { it.name })
        assertEquals("-500 USD", byChange.rows[0].converted?.plain())
        assertEquals("-100 USD", byChange.rows[1].converted?.plain())
        assertNull(byChange.rows[2].converted)
    }

    @Test
    fun `personal accounts are labelled only with other members around`() {
        val alone = build(CapitalView(YearMonth(2026, 10))).groups[0].rows.first { it.id == "ameria" }
        assertNull(alone.personalOf)
        val family = build(CapitalView(YearMonth(2026, 10)), others = mapOf("anya" to "Аня")).groups[0].rows
        assertEquals("Аня", family.first { it.id == "ameria" }.personalOf)
        assertNull(family.first { it.id == "galicia" }.personalOf)
    }

    @Test
    fun `nothing counted — an empty screen`() {
        val state = buildCapitalMonth(input().copy(accounts = listOf(hidden)), CapitalView(YearMonth(2026, 10)), format, format)
        assertTrue(state.isEmpty)
        assertTrue(state.points.isEmpty())
    }
}
