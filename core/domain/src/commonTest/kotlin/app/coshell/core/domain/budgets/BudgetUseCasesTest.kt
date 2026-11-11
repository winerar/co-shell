package app.coshell.core.domain.budgets

import app.coshell.core.domain.FakeBudgetRepository
import app.coshell.core.domain.FakeCategoryRepository
import app.coshell.core.domain.budgets.SaveBudgetUseCase.Reason
import app.coshell.core.domain.budgets.SaveBudgetUseCase.Result
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.budgetId
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BudgetUseCasesTest {
    private val categories = FakeCategoryRepository()
    private val budgets = FakeBudgetRepository()
    private val save = SaveBudgetUseCase(categories, budgets)
    private val remove = RemoveBudgetUseCase(budgets)
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")

    private val september = YearMonth(2026, 9)
    private val october = YearMonth(2026, 10)
    private val november = YearMonth(2026, 11)
    private val december = YearMonth(2026, 12)

    init {
        categories.items.value = listOf(
            Category("home", "Дом", CategoryType.EXPENSE),
            Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
            Category("salary", "Зарплата", CategoryType.INCOME),
            Category("old", "Старое", CategoryType.EXPENSE, archived = true),
        ).associateBy { it.id }
    }

    private suspend fun limitOf(categoryId: String, month: YearMonth) =
        budgets.getBudgets().forCategory(categoryId, month)?.limit

    @Test
    fun `a limit on a folder repeats every month from its first one`() = runTest {
        val result = save("home", october, Money(400_000_00, ars), note = "  Аренда до 5-го ", rollover = true)

        val saved = (result as Result.Saved).budget
        assertEquals(Budget(budgetId("home", october), "home", october, Money(400_000_00, ars), "Аренда до 5-го", true), saved)
        assertNull(limitOf("home", september))
        assertEquals(Money(400_000_00, ars), limitOf("home", october))
        assertEquals(Money(400_000_00, ars), limitOf("home", YearMonth(2028, 1)))
    }

    @Test
    fun `changing the amount later keeps the old limit in past months`() = runTest {
        save("home", october, Money(400_000_00, ars))

        save("home", december, Money(450_000_00, ars))

        assertEquals(Money(400_000_00, ars), limitOf("home", november))
        assertEquals(Money(450_000_00, ars), limitOf("home", december))
    }

    @Test
    fun `saving in the same month replaces the limit, currency and note`() = runTest {
        save("home", october, Money(400_000_00, ars), note = "черновик")

        save("home", october, Money(350_00, usd))

        val only = budgets.getBudgets().single()
        assertEquals(Money(350_00, usd), only.limit)
        assertNull(only.note)
    }

    @Test
    fun `a record of the same month made under another id is replaced`() = runTest {
        budgets.applyBudgets(save = listOf(Budget("random-id", "home", october, Money(100_00, ars))))

        save("home", october, Money(400_000_00, ars))

        assertEquals(listOf(budgetId("home", october)), budgets.getBudgets().map { it.id })
    }

    @Test
    fun `a category inside a folder may have its own limit next to the folder limit`() = runTest {
        save("home", october, Money(400_000_00, ars))
        save("rent", october, Money(280_000_00, ars))

        assertEquals(setOf("home", "rent"), budgets.getBudgets().inMonth(october).map { it.categoryId }.toSet())
    }

    @Test
    fun `limits are refused for income, archived, missing and adjustment categories and for zero`() = runTest {
        val limit = Money(100_00, ars)
        assertEquals(Result.Invalid(Reason.NOT_EXPENSE), save("salary", october, limit))
        assertEquals(Result.Invalid(Reason.CATEGORY_NOT_FOUND), save("old", october, limit))
        assertEquals(Result.Invalid(Reason.CATEGORY_NOT_FOUND), save("nope", october, limit))
        assertEquals(Result.Invalid(Reason.SYSTEM_CATEGORY), save(SystemCategories.ADJUSTMENT_EXPENSE_ID, october, limit))
        assertEquals(Result.Invalid(Reason.AMOUNT_NOT_POSITIVE), save("home", october, Money(0, ars)))
        assertEquals(emptyList(), budgets.getBudgets())
    }

    @Test
    fun `removing from a month keeps the plan of the months before`() = runTest {
        save("home", september, Money(300_000_00, ars))
        save("home", november, Money(400_000_00, ars))

        remove("home", november)

        assertEquals(Money(300_000_00, ars), limitOf("home", october))
        assertNull(limitOf("home", november))
        assertNull(limitOf("home", december))
    }

    @Test
    fun `removing in the middle ends the running limit at the previous month`() = runTest {
        save("home", september, Money(300_000_00, ars))

        remove("home", november)

        assertEquals(october, budgets.getBudgets().single().lastMonth)
        assertEquals(Money(300_000_00, ars), limitOf("home", october))
        assertNull(limitOf("home", november))
    }

    @Test
    fun `a limit set again after removal does not bring back the months in between`() = runTest {
        save("home", september, Money(300_000_00, ars))
        remove("home", october)

        save("home", december, Money(500_000_00, ars))

        assertEquals(Money(300_000_00, ars), limitOf("home", september))
        assertNull(limitOf("home", october))
        assertNull(limitOf("home", november))
        assertEquals(Money(500_000_00, ars), limitOf("home", december))
    }

    @Test
    fun `removing touches only that category`() = runTest {
        save("home", october, Money(400_000_00, ars))
        save("rent", october, Money(280_000_00, ars))

        remove("rent", october)

        assertEquals(listOf("home"), budgets.getBudgets().map { it.categoryId })
    }
}
