package app.coshell.core.domain.categoryguess

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MccCategoriesTest {
    private val categories = listOf(
        Category("food", "Продукты", CategoryType.EXPENSE, icon = "food"),
        Category("cafe", "Кафе", CategoryType.EXPENSE, icon = "restaurant"),
    )

    @Test
    fun `plain codes have a category, banks and unknown ones none`() {
        assertEquals("food", MccCategories.iconOf("5411"))
        assertEquals("restaurant", MccCategories.iconOf("5812"))
        assertEquals("taxi", MccCategories.iconOf("4121"))
        assertNull(MccCategories.iconOf("6011"))
        assertNull(MccCategories.iconOf(null))
    }

    @Test
    fun `codes belong to one category each`() {
        val all = listOf("5411", "5812", "4121", "5541", "5912", "5651", "5641", "5200", "4900", "5815", "8220", "7832", "5992")
        assertEquals(all.size, all.mapNotNull(MccCategories::iconOf).size)
    }

    @Test
    fun `an unknown shop of a payment QR gets the category of its code`() {
        val guess = CategoryGuess.suggest(emptyList(), TransactionType.EXPENSE, categories, payee = "LA ESQUINA SRL", mcc = "5812")!!
        assertEquals("cafe", guess.categoryId)
        assertEquals(CategoryGuess.Source.DICTIONARY, guess.source)
        assertNull(CategoryGuess.suggest(emptyList(), TransactionType.INCOME, categories, payee = "LA ESQUINA SRL", mcc = "5812"))
    }

    @Test
    fun `the seller of a receipt key`() {
        assertEquals("30685849751", CategoryGuess.seller("30685849751:442:6:117817"))
        assertEquals("fns:7380440700076549", CategoryGuess.seller("fns:7380440700076549:12345:1234567890"))
        assertEquals("teleshka", CategoryGuess.seller("teleshka:42"))
        assertNull(CategoryGuess.seller("emv:00ff"))
        assertNull(CategoryGuess.seller("text:00ff"))
    }
}
