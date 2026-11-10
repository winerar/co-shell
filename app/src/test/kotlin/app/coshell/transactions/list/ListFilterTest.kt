package app.coshell.transactions.list

import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.transactions.list.TransactionsUiState.Kind
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListFilterTest {
    private val ars = CurrencyCode("ARS")
    private val zone = TimeZone.of("America/Argentina/Buenos_Aires")
    private val accounts = mapOf("card" to Account("card", "Galicia", ars))
    private val categories = mapOf("food" to Category("food", "Продукты", CategoryType.EXPENSE))
    private val coto = Transaction(
        "1", TransactionType.EXPENSE, "card", Money(32_000_00, ars), Instant.fromEpochSeconds(0), categoryId = "food", note = "Coto",
    )

    private fun ListFilter.hit(t: Transaction = coto) = matches(t, accounts, categories, MoneyFormat.RU)

    @Test
    fun queryMatchesNoteCategoryAccountAndAmount() {
        assertTrue(ListFilter(query = "coto").hit())
        assertTrue(ListFilter(query = "продук").hit())
        assertTrue(ListFilter(query = "galicia").hit())
        assertTrue(ListFilter(query = "32000").hit())
        assertFalse(ListFilter(query = "такси").hit())
    }

    @Test
    fun kindFilters() {
        assertTrue(ListFilter(kind = Kind.EXPENSE).hit())
        assertFalse(ListFilter(kind = Kind.INCOME).hit())
    }

    @Test
    fun periodAndAccountGoToRoomAndSearchDropsThePage() {
        val today = LocalDate(2026, 11, 9)
        val f = ListFilter(accountId = "card", period = ListFilter.Period.LAST_MONTH).toRepositoryFilter(200, today, zone)
        assertEquals("card", f.accountId)
        assertEquals(LocalDateTime(2026, 10, 1, 0, 0).toInstant(zone), f.from)
        assertEquals(LocalDateTime(2026, 11, 1, 0, 0).toInstant(zone), f.to)
        assertEquals(200, f.limit)
        assertNull(ListFilter(query = "x").toRepositoryFilter(200, today, zone).limit)
    }

    @Test
    fun uncategorizedKeepsOnlyExpensesAndIncomesWithoutCategory() {
        val f = ListFilter.UncategorizedThisMonth
        assertFalse(f.hit())
        assertTrue(f.hit(coto.copy(categoryId = null)))
        assertTrue(f.hit(coto.copy(type = TransactionType.INCOME, categoryId = null)))
        assertFalse(f.hit(coto.copy(type = TransactionType.TRANSFER, categoryId = null, toAccountId = "cash", toAmount = coto.amount)))
        assertTrue(f.isActive)
        assertEquals(ListFilter.Period.THIS_MONTH, f.period)
        // Checked in memory, so Room must return the whole month, not a page.
        assertNull(f.toRepositoryFilter(200, LocalDate(2026, 11, 9), zone).limit)
    }
}
