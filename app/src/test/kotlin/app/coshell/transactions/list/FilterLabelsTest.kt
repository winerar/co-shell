package app.coshell.transactions.list

import app.coshell.core.domain.search.AmountQuery
import app.coshell.core.domain.search.PeriodRange
import app.coshell.core.domain.search.PeriodRange.Preset
import app.coshell.transactions.list.FilterChoices.AccountChoice
import app.coshell.transactions.list.FilterChoices.CategoryChoice
import app.coshell.transactions.list.FilterChoices.MemberChoice
import app.coshell.transactions.list.FilterChoices.TagChoice
import java.util.Locale
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FilterLabelsTest {
    private val food = CategoryChoice(
        "food", "Еда", null, null,
        children = listOf(CategoryChoice("shop", "Продукты", null, null), CategoryChoice("bakery", "Пекарня", null, null), CategoryChoice("cafe", "Кафе", null, null)),
    )
    private val taxi = CategoryChoice("taxi", "Такси", null, null)
    private val choices = FilterChoices(
        accounts = listOf(AccountChoice("galicia", "Galicia", "ARS", null), AccountChoice("usdt", "USDT-карта", "USDT", null)),
        expenseCategories = listOf(food, taxi),
        members = listOf(MemberChoice("anya", "Аня")),
    )
    private val ru = Locale.forLanguageTag("ru")
    private val today = LocalDate(2026, 11, 12)

    private fun cats(ids: Set<String>, uncategorized: Boolean = false) =
        FilterLabels.categories(ListFilter(categoryIds = ids, uncategorized = uncategorized), choices, "Без категории")

    @Test
    fun categoryChip() {
        assertNull(cats(emptySet()))
        assertEquals("Без категории", cats(emptySet(), uncategorized = true))
        assertEquals("Такси", cats(setOf("taxi")))
        assertEquals("Пекарня", cats(setOf("bakery")))
        assertEquals("Еда", cats(food.ids))
        assertEquals("Еда · 2", cats(setOf("shop", "bakery")))
        assertEquals("Еда · 4", cats(food.ids + "taxi"))
        assertEquals("Такси · 2", cats(setOf("taxi"), uncategorized = true))
    }

    @Test
    fun accountAndWhoChips() {
        assertNull(FilterLabels.accounts(ListFilter(), choices))
        assertEquals("USDT-карта", FilterLabels.accounts(ListFilter(accountIds = setOf("usdt")), choices))
        assertEquals("Galicia · 2", FilterLabels.accounts(ListFilter(accountIds = setOf("usdt", "galicia")), choices))
        assertEquals("Я", FilterLabels.who(ListFilter(who = ListFilter.Who.Me), choices, "Я"))
        assertEquals("Аня", FilterLabels.who(ListFilter(who = ListFilter.Who.Member("anya")), choices, "Я"))
        assertNull(FilterLabels.who(ListFilter(), choices, "Я"))
    }

    @Test
    fun periodChip() {
        assertEquals("Ноябрь", FilterLabels.period(Preset.THIS_MONTH.range(today), today, ru))
        assertEquals("Ноябрь 2025", FilterLabels.period(Preset.THIS_MONTH.range(today).shifted(-12), today, ru))
        assertEquals("2025", FilterLabels.period(Preset.LAST_YEAR.range(today), today, ru))
        assertEquals("Сент – нояб", FilterLabels.period(Preset.THREE_MONTHS.range(today), today, ru))
        assertEquals("1 нояб. – 7 нояб.", FilterLabels.period(PeriodRange.days(LocalDate(2026, 11, 1), LocalDate(2026, 11, 7)), today, ru))
        assertEquals("3 окт. 2025", FilterLabels.period(PeriodRange.days(LocalDate(2025, 10, 3), LocalDate(2025, 10, 3)), today, ru))
    }

    @Test
    fun amountBounds() {
        assertEquals(listOf("3 000", "5 000"), FilterLabels.amountBounds(AmountQuery.parse("3000-5000")!!))
        assertEquals(listOf("41,20"), FilterLabels.amountBounds(AmountQuery.parse("=41,2")!!))
        assertEquals(listOf("1 000 000"), FilterLabels.amountBounds(AmountQuery.parse(">1000000")!!))
    }

    @Test
    fun tagChipShowsTheFirstNameAndTheCount() {
        val tags = choices.copy(tags = listOf(TagChoice("аня", "Аня", 0, 3), TagChoice("кот", "кот", 0, 1), TagChoice("миша", "Миша", 0, 2)))
        assertNull(FilterLabels.tags(ListFilter(), tags))
        assertEquals("# кот", FilterLabels.tags(ListFilter(tagIds = setOf("кот")), tags))
        assertEquals("# Аня · 2", FilterLabels.tags(ListFilter(tagIds = setOf("миша", "аня")), tags))
        assertNull(FilterLabels.tags(ListFilter(tagIds = setOf("deleted")), tags))
    }
}
