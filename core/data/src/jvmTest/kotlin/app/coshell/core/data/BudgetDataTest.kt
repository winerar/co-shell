package app.coshell.core.data

import app.cash.turbine.test
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomBudgetRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.repository.toModel
import app.coshell.core.data.sync.SyncCodec
import app.coshell.core.data.sync.SyncKind
import app.coshell.core.data.sync.SyncRecord
import app.coshell.core.model.Budget
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.budgetId
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BudgetDataTest {
    private val db = createInMemoryCoShellDatabase()
    private val budgets = RoomBudgetRepository(db.budgetDao(), WriteStamper())
    private val ars = CurrencyCode("ARS")
    private val november = YearMonth(2026, 11)

    private fun home(limit: Long = 400_000_00, month: YearMonth = november) =
        Budget(budgetId("home", month), "home", month, Money(limit, ars))

    @Test
    fun `a limit keeps its note, rollover and last month`() = runTest {
        val budget = home().copy(note = "Аренда 280 000 до 5-го", rollover = true, lastMonth = YearMonth(2027, 3))

        budgets.applyBudgets(save = listOf(budget))

        assertEquals(listOf(budget), budgets.getBudgets())
    }

    @Test
    fun `a limit synced from a version without the new fields repeats without note, rollover or end`() {
        val old = """{"id":"b1","categoryId":"home","month":"2026-11","limitMinor":40000000,"currency":"ARS","updatedAt":1,"updatedBy":null,"deleted":false}"""

        val budget = SyncCodec.budget(SyncRecord(SyncKind.BUDGET, "b1", 1, null, false, old)).toModel()

        assertEquals(Budget("b1", "home", november, Money(400_000_00, ars)), budget)
    }

    @Test
    fun `deleted limits disappear from the list and are marked for sync`() = runTest {
        budgets.applyBudgets(save = listOf(home(), home(month = YearMonth(2027, 1))))

        budgets.observeBudgets().test {
            assertEquals(2, awaitItem().size)
            budgets.applyBudgets(deleteIds = listOf(budgetId("home", november)))
            assertEquals(listOf(YearMonth(2027, 1)), awaitItem().map { it.startMonth })
        }
        assertTrue(db.syncDao().pendingBudgets().single { it.id == home().id }.deleted)
    }

    @Test
    fun `saving a limit again under the id of a deleted one brings it back`() = runTest {
        budgets.applyBudgets(save = listOf(home()))
        budgets.applyBudgets(deleteIds = listOf(home().id))

        budgets.applyBudgets(save = listOf(home(limit = 450_000_00)), deleteIds = listOf(home().id))

        assertEquals(listOf(Money(450_000_00, ars)), budgets.getBudgets().map { it.limit })
    }
}
