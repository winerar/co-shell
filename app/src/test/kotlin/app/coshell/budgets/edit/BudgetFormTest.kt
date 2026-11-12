package app.coshell.budgets.edit

import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.budgetId
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetFormTest {
    private val ars = CurrencyCode("ARS")
    private val november = YearMonth(2026, 11)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)

    private val categories = listOf(
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
        Category("utilities", "Коммуналка", CategoryType.EXPENSE, parentId = "home"),
        Category("old", "Старое", CategoryType.EXPENSE, parentId = "home", archived = true),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("empty", "Пустая папка", CategoryType.EXPENSE),
        Category("trip", "Поездки", CategoryType.EXPENSE),
        Category("gone", "Архивная", CategoryType.EXPENSE, parentId = "trip", archived = true),
        Category("salary", "Зарплата", CategoryType.INCOME),
        Category(SystemCategories.ADJUSTMENT_EXPENSE_ID, "Корректировка", CategoryType.EXPENSE),
    )

    @Test
    fun `folders are expense categories with live subcategories, the rest are plain categories`() {
        val (folders, plain) = budgetChoices(categories)
        assertEquals(listOf("home"), folders.map { it.id })
        assertEquals(listOf("Дом / Аренда", "Дом / Коммуналка", "Кафе", "Поездки", "Пустая папка"), plain.map { it.label })
    }

    @Test
    fun `inside a folder — live subcategories with their own limit of the month`() {
        val budgets = listOf(Budget(budgetId("utilities", november), "utilities", november, Money.ofMajor(70_000, currency = ars)))
        val inside = insideFolder("home", categories, budgets, november, format)
        assertEquals(listOf("Аренда", "Коммуналка"), inside.map { it.name })
        assertNull(inside[0].limit)
        assertEquals(format.format(Money.ofMajor(70_000, currency = ars)), inside[1].limit)
    }

    @Test
    fun `typed limit must be a positive number in the currency`() {
        assertEquals(Money.ofMajor(400_000, currency = ars), parseLimit("400 000", "ARS"))
        assertEquals(Money.ofMajor(12, 50, ars), parseLimit("12,5", "ARS"))
        assertNull(parseLimit("", "ARS"))
        assertNull(parseLimit("0", "ARS"))
        assertNull(parseLimit("-5", "ARS"))
        assertNull(parseLimit("abc", "ARS"))
        assertNull(parseLimit("100", "??"))
    }

    @Test
    fun `currencies — report ones first, then accounts and the current limit, no repeats`() {
        val usd = CurrencyCode("USD")
        val amd = CurrencyCode("AMD")
        assertEquals(listOf("USD", "ARS", "AMD"), limitCurrencies(listOf(usd, ars), listOf(ars, usd), amd))
    }
}
