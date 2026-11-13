package app.coshell.transactions.list

import app.coshell.core.domain.AuthorFilter
import app.coshell.core.domain.search.PeriodRange
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Tag
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
    fun searchAlsoFindsPayeeAndLabels() {
        val tagged = coto.copy(payee = "Café Martínez", labels = listOf("отпуск", "дети"))
        assertTrue(ListFilter(query = "martínez").hit(tagged))
        assertTrue(ListFilter(query = "отпуск").hit(tagged))
        // Two labels are two fields: a query never matches across them.
        assertFalse(ListFilter(query = "отпускдети").hit(tagged))
    }

    @Test
    fun anAmountQueryComparesAmountsInsteadOfText() {
        assertTrue(ListFilter(query = "=32000").hit())
        assertTrue(ListFilter(query = "30000-40000").hit())
        assertFalse(ListFilter(query = ">40000").hit())
        val paidInUsdt = coto.copy(amount = Money(41_20, CurrencyCode("USDT")), originalAmount = Money(42_000_00, ars))
        // Either amount may fit: the account's (41,20 USDT) or the receipt's (42 000 ARS).
        assertTrue(ListFilter(query = "<50").hit(paidInUsdt))
        assertTrue(ListFilter(query = "=42000").hit(paidInUsdt))
        assertFalse(ListFilter(query = "100-1000").hit(paidInUsdt))
    }

    @Test
    fun accountsAndCategoriesAreCheckedAndAddUp() {
        assertTrue(ListFilter(accountIds = setOf("card", "cash")).hit())
        assertFalse(ListFilter(accountIds = setOf("cash")).hit())
        assertTrue(ListFilter(accountIds = setOf("cash")).hit(coto.copy(type = TransactionType.TRANSFER, categoryId = null, toAccountId = "cash", toAmount = coto.amount)))
        assertTrue(ListFilter(categoryIds = setOf("food")).hit())
        assertFalse(ListFilter(categoryIds = setOf("cafe")).hit())
        assertTrue(ListFilter(categoryIds = setOf("cafe"), uncategorized = true).hit(coto.copy(categoryId = null)))
    }

    @Test
    fun everythingButTheTextGoesToRoomAndAnyFilterDropsThePage() {
        val october = PeriodRange.Preset.LAST_MONTH.range(LocalDate(2026, 11, 9))
        val f = ListFilter(accountIds = setOf("card"), categoryIds = setOf("food"), period = october, who = ListFilter.Who.Me)
            .toRepositoryFilter(200, zone, others = setOf("anya"))
        assertEquals(setOf("card"), f.accountIds)
        assertEquals(setOf("food"), f.categoryIds)
        assertEquals(LocalDateTime(2026, 10, 1, 0, 0).toInstant(zone), f.from)
        assertEquals(LocalDateTime(2026, 11, 1, 0, 0).toInstant(zone), f.to)
        assertEquals(AuthorFilter.Me(setOf("anya")), f.author)
        assertNull(f.limit)
        assertEquals(AuthorFilter.Member("anya"), ListFilter(who = ListFilter.Who.Member("anya")).toRepositoryFilter(200, zone).author)
        assertNull(ListFilter(query = "x").toRepositoryFilter(200, zone).limit)
        val plain = ListFilter().toRepositoryFilter(200, zone)
        assertEquals(200, plain.limit)
        assertNull(plain.accountIds)
        assertNull(plain.categoryIds)
    }

    @Test
    fun theRoomKeyIgnoresTypedLetters() {
        assertEquals(ListFilter(query = "т").roomKey, ListFilter(query = "так").roomKey)
        assertTrue(ListFilter(query = "т").roomKey != ListFilter().roomKey)
    }

    @Test
    fun uncategorizedKeepsOnlyExpensesAndIncomesWithoutCategory() {
        val today = LocalDate(2026, 11, 9)
        val f = ListFilter.uncategorizedThisMonth(today)
        assertFalse(f.hit())
        assertTrue(f.hit(coto.copy(categoryId = null)))
        assertTrue(f.hit(coto.copy(type = TransactionType.INCOME, categoryId = null)))
        assertFalse(f.hit(coto.copy(type = TransactionType.TRANSFER, categoryId = null, toAccountId = "cash", toAmount = coto.amount)))
        assertTrue(f.isActive)
        assertEquals(PeriodRange.Preset.THIS_MONTH.range(today), f.period)
        assertTrue(f.toRepositoryFilter(200, zone).uncategorized)
    }

    @Test
    fun tagFilterKeepsOperationsWithAnyChosenTagAndStaysOutOfRoom() {
        val tagged = coto.copy(labels = listOf("Испанский", "Аня"))
        assertTrue(ListFilter(tagIds = setOf("аня")).hit(tagged))
        assertTrue(ListFilter(tagIds = setOf("кот", "испанский")).hit(tagged))
        assertFalse(ListFilter(tagIds = setOf("кот")).hit(tagged))
        assertFalse(ListFilter(tagIds = setOf("аня")).hit(coto))
        assertTrue(ListFilter(tagIds = setOf("аня")).isActive)
        // Another tag choice must not re-query Room.
        assertEquals(ListFilter(tagIds = setOf("аня")).roomKey, ListFilter(tagIds = setOf("кот", "миша")).roomKey)
    }

    @Test
    fun searchFindsATagByNameWhenTheLabelIsAnId() {
        val id = "0f8fad5b-d9cb-469f-a165-70867728950e"
        val index = SearchIndex(accounts, categories, MoneyFormat.RU, mapOf(id to Tag(id, "Барилоче")))
        assertTrue(ListFilter(query = "барил").matches(coto.copy(labels = listOf(id)), index))
        assertFalse(ListFilter(query = "барил").matches(coto.copy(id = "2"), index))
    }
}
