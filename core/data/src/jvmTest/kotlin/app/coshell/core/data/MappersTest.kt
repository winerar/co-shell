package app.coshell.core.data

import app.coshell.core.data.db.toEntity
import app.coshell.core.data.db.toModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MappersTest {
    @Test
    fun `account survives a round trip`() {
        assertEquals(card, card.toEntity().toModel())
        assertEquals(cash, cash.toEntity().toModel())
    }

    @Test
    fun `category survives a round trip`() {
        assertEquals(cafe, cafe.toEntity().toModel())
    }

    @Test
    fun `transaction with every optional field survives a round trip`() {
        assertEquals(coffee, coffee.toEntity().toModel())
        assertEquals(exchange, exchange.toEntity().toModel())
        val noLabels = coffee.copy(labels = emptyList())
        assertEquals(noLabels, noLabels.toEntity().toModel())
    }

    @Test
    fun `stores date as sortable iso text`() {
        assertEquals("2026-11-08", coffee.toEntity().date)
    }

    @Test
    fun `refuses labels with the reserved separator`() {
        assertFailsWith<IllegalArgumentException> { coffee.copy(labels = listOf("a\u001Fb")).toEntity() }
    }
}
