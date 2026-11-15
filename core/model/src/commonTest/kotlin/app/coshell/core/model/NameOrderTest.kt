package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class NameOrderTest {
    private fun account(id: String, name: String, archived: Boolean = false, order: Int = 0) =
        Account(id, name, CurrencyCode("ARS"), archived = archived, sortOrder = order)

    @Test
    fun keyIgnoresCaseSpacesAndYo() {
        assertEquals("елка", nameSortKey("  Ёлка "))
        assertEquals(nameSortKey("galicia"), nameSortKey("Galicia"))
    }

    @Test
    fun accountsAreAlphabeticalWithArchivedLast() {
        val sorted = listOf(
            account("z", "Наличные", order = 0),
            account("old", "Альфа", archived = true),
            account("mp", "mercado pago", order = 2),
            account("g", "Galicia", order = 1),
            account("e", "Ёж-карта", order = 3),
        ).sortedWith(ACCOUNT_ORDER)
        assertEquals(listOf("g", "mp", "e", "z", "old"), sorted.map { it.id })
    }
}
