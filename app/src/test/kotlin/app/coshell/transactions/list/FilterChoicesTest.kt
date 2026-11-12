package app.coshell.transactions.list

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.search.FilterTotal
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.transactions.list.FilterChoices.CategoryChoice
import app.coshell.transactions.list.TransactionsUiState.Kind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FilterChoicesTest {
    private val food = CategoryChoice(
        "food", "Еда", null, null,
        children = listOf(CategoryChoice("shop", "Продукты", null, null), CategoryChoice("cafe", "Кафе", null, null)),
    )
    private val choices = FilterChoices(expenseCategories = listOf(food, CategoryChoice("taxi", "Такси", null, null)))

    @Test
    fun aFolderTogglesWithAllItsCategoriesAndFollowsThem() {
        val all = choices.toggle(emptySet(), food)
        assertEquals(setOf("food", "shop", "cafe"), all)
        assertEquals(emptySet<String>(), choices.toggle(all, food))

        // One child off — the folder's own id goes too; both on again — it comes back.
        val oneOff = choices.toggle(all, food.children[1])
        assertEquals(setOf("shop"), oneOff)
        assertEquals(1, choices.chosenIn(food, oneOff))
        assertEquals(all, choices.toggle(oneOff, food.children[1]))
        // A partly chosen folder: a tap chooses all of it.
        assertEquals(all, choices.toggle(oneOff, food))
    }

    @Test
    fun choicesKeepArchivedOnlyWhileChosen() {
        val ars = CurrencyCode("ARS")
        val accounts = listOf(Account("a", "Galicia", ars), Account("old", "Старая", ars, archived = true))
        val categories = listOf(
            Category("food", "Еда", CategoryType.EXPENSE),
            Category("shop", "Продукты", CategoryType.EXPENSE, parentId = "food"),
            Category("gone", "Старое", CategoryType.EXPENSE, archived = true),
            Category("salary", "Зарплата", CategoryType.INCOME),
        )
        val plain = buildFilterChoices(accounts, categories, mapOf("anya" to "Аня"), ListFilter())
        assertEquals(listOf("a"), plain.accounts.map { it.id })
        assertEquals(listOf("food"), plain.expenseCategories.map { it.id })
        assertEquals(listOf("shop"), plain.expenseCategories.single().children.map { it.id })
        assertEquals(listOf("salary"), plain.incomeCategories.map { it.id })
        assertEquals(listOf("Аня"), plain.members.map { it.name })
        assertEquals(3, plain.categoryCount)

        val chosen = buildFilterChoices(accounts, categories, emptyMap(), ListFilter(accountIds = setOf("old"), categoryIds = setOf("gone")))
        assertEquals(listOf("a", "old"), chosen.accounts.map { it.id })
        assertEquals(listOf("food", "gone"), chosen.expenseCategories.map { it.id })
    }

    @Test
    fun totalTexts() {
        val ars = CurrencyCode("ARS")
        val usd = CurrencyCode("USD")
        val amd = CurrencyCode("AMD")
        val f = MoneyFormat.RU
        fun m(major: Long, c: CurrencyCode) = Money.ofMajor(major, currency = c)

        val one = FilterTotal(3, listOf(m(-84_300, ars)), CurrencyConverter.Total(m(-82, usd), emptySet())).toUi(f)
        assertEquals(f.format(m(-84_300, ars)), one.amount)
        assertEquals("≈ " + f.format(m(-82, usd)), one.detail)
        assertEquals(Kind.EXPENSE, one.tone)

        val sameAsMain = FilterTotal(1, listOf(m(5, usd)), CurrencyConverter.Total(m(5, usd), emptySet())).toUi(f)
        assertEquals("+" + f.format(m(5, usd)), sameAsMain.amount)
        assertNull(sameAsMain.detail)
        assertEquals(Kind.INCOME, sameAsMain.tone)

        val several = FilterTotal(5, listOf(m(-1000, ars), m(-500, amd)), CurrencyConverter.Total(m(-1, usd), setOf(amd))).toUi(f)
        assertEquals("≈ " + f.format(m(-1, usd)), several.amount)
        assertEquals(f.format(m(-1000, ars)) + "\n" + f.format(m(-500, amd)), several.detail)
        assertEquals("AMD", several.noRate)

        val transfers = FilterTotal(2, emptyList(), null).toUi(f)
        assertEquals(2, transfers.count)
        assertNull(transfers.amount)
    }
}
