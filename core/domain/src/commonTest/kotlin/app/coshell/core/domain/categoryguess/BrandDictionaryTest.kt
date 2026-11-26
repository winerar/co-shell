package app.coshell.core.domain.categoryguess

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BrandDictionaryTest {
    private fun icon(text: String) = BrandDictionary.iconOf(text)

    @Test
    fun `known brands give the icon of their kind`() {
        assertEquals("food", icon("Disco"))
        assertEquals("food", icon("Carrefour Express"))
        assertEquals("restaurant", icon("Starbucks"))
        assertEquals("taxi", icon("Uber"))
        assertEquals("health", icon("Farmacity"))
        assertEquals("utilities", icon("Edenor"))
        assertEquals("subscriptions", icon("Netflix"))
        assertEquals("transport", icon("YPF"))
        assertEquals("food", icon("Пятёрочка"))
        assertEquals("taxi", icon("Яндекс Go"))
    }

    @Test
    fun `kinds of places work for payees nobody has heard of`() {
        assertEquals("food", icon("Panadería La Estrella"))
        assertEquals("health", icon("Dentista Rojas"))
        assertEquals("health", icon("Аптека 36,6"))
        assertEquals("home", icon("Easy ferretería"))
        assertEquals("restaurant", icon("Pizzería Don Juan"))
    }

    @Test
    fun `russia and cis brands and everyday words are known`() {
        assertEquals("food", icon("Yerevan City"))
        assertEquals("food", icon("Magnum"))
        assertEquals("food", icon("Евроопт"))
        assertEquals("home", icon("Wildberries"))
        assertEquals("restaurant", icon("кофе"))
        assertEquals("transport", icon("бензин"))
        assertEquals("restaurant", icon("almuerzo"))
        assertEquals("taxi", icon("такси"))
    }

    @Test
    fun `misspelled or transliterated names are matched loosely`() {
        assertEquals("food", icon("карфур"))
        assertEquals("food", icon("carrefor"))
        assertEquals("health", icon("в аптеке"))
        assertEquals("transport", icon("заправке"))
    }

    @Test
    fun `words of the owner's own notes are known (#742 gold set)`() {
        assertEquals("transport", icon("Субе"))
        assertEquals("food", icon("вода"))
        assertEquals("restaurant", icon("десерты"))
        assertEquals("utilities", icon("вода за месяц"))
    }

    @Test
    fun `phrases that are not purchases stay unknown`() {
        assertNull(icon("тест заметки"))
        assertNull(icon("позвонить маме"))
        assertNull(icon("hola"))
    }

    @Test
    fun `bank noise and digits do not matter`() {
        assertEquals("food", icon("MERPAGO*KIOSCO 123"))
        assertEquals("taxi", icon("MP*UBER TRIP 8841"))
        assertEquals("food", icon("  disco  "))
    }

    @Test
    fun `the longest match wins`() {
        assertEquals("restaurant", icon("Uber Eats"))
        assertEquals("taxi", icon("Uber"))
        assertEquals("restaurant", icon("Café Martínez"))
    }

    @Test
    fun `short and common names must be the whole payee`() {
        assertEquals("food", icon("Dia"))
        assertEquals("food", icon("Dia 123"))
        assertNull(icon("Dia del Niño Fiesta"))
        assertEquals("subscriptions", icon("Personal"))
        assertNull(icon("Personal trainer Marcos"))
    }

    @Test
    fun `unknown payees and blanks give nothing`() {
        assertNull(icon("Juan Pérez"))
        assertNull(icon(""))
        assertNull(BrandDictionary.iconOf(null))
    }

    @Test
    fun `the category is the persons own with that icon not an archived one`() {
        val categories = listOf(
            Category("a", "Мои продукты", CategoryType.EXPENSE, icon = "food", sortOrder = 2),
            Category("b", "Еда", CategoryType.EXPENSE, icon = "food", sortOrder = 1),
            Category("c", "Старое кафе", CategoryType.EXPENSE, icon = "restaurant", archived = true),
            Category("d", "Зарплата", CategoryType.INCOME, icon = "food"),
        )
        assertEquals("b", BrandDictionary.categoryFor("food", categories))
        assertNull(BrandDictionary.categoryFor("restaurant", categories))
        assertNull(BrandDictionary.categoryFor(null, categories))
        assertNull(BrandDictionary.categoryFor("taxi", categories))
    }

    @Test
    fun `the gold payees of the model test are mostly found`() {
        // Optimistic by construction: the dictionary was written by someone who had seen this list (docs/research/on-device-ai.md).
        val gold = mapOf(
            "Disco" to "food", "Coto" to "food", "Jumbo" to "food", "Dia" to "food", "Лента" to "food", "Kiosco 24hs" to "food", "Starbucks" to "restaurant",
            "Havanna" to "restaurant", "McDonald's" to "restaurant", "Burger King" to "restaurant", "Freddo" to "restaurant", "Rappi" to "restaurant", "YPF" to "transport",
            "Shell Select" to "transport", "SUBE recarga" to "transport", "Uber" to "taxi", "Cabify" to "taxi", "Яндекс Go" to "taxi", "Farmacity" to "health",
            "Swiss Medical" to "health", "Edenor" to "utilities", "Metrogas" to "utilities", "Aysa" to "utilities", "Cinemark" to "fun", "Steam" to "fun",
            "Netflix" to "subscriptions", "Spotify" to "subscriptions", "Zara" to "clothes", "H&M" to "clothes", "Claro" to "subscriptions", "МТС" to "subscriptions",
            "Florería Rosa" to "gift", "Easy ferretería" to "home",
        )
        val right = gold.count { (name, icon) -> icon(name) == icon }
        assertTrue(right >= gold.size * 9 / 10, "found $right of ${gold.size}")
    }
}
