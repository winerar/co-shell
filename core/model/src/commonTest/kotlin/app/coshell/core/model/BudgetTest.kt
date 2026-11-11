package app.coshell.core.model

import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BudgetTest {
    private val ars = CurrencyCode("ARS")
    private val october = YearMonth(2026, 10)
    private fun budget(lastMonth: YearMonth? = null) =
        Budget("b", "home", october, Money(400_000_00, ars), lastMonth = lastMonth)

    @Test
    fun `a limit without an end covers its first month and every month after`() {
        assertFalse(budget().covers(YearMonth(2026, 9)))
        assertTrue(budget().covers(october))
        assertTrue(budget().covers(YearMonth(2030, 1)))
    }

    @Test
    fun `an ended limit covers months up to the last one inclusive`() {
        val ended = budget(lastMonth = YearMonth(2026, 12))
        assertTrue(ended.covers(YearMonth(2026, 12)))
        assertFalse(ended.covers(YearMonth(2027, 1)))
    }

    @Test
    fun `a limit that ends before it starts never applies`() {
        assertFalse(budget(lastMonth = YearMonth(2026, 9)).covers(october))
    }

    @Test
    fun `limit must be positive and a note must not be blank`() {
        assertFailsWith<IllegalArgumentException> { Budget("b", "home", october, Money(0, ars)) }
        assertFailsWith<IllegalArgumentException> { Budget("b", "home", october, Money(1, ars), note = " ") }
    }

    @Test
    fun `the id is the same for the same category and month on any phone`() {
        assertEquals("home:2026-10", budgetId("home", october))
    }
}
