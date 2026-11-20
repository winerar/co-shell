package app.coshell.core.domain.receipts

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class ReceiptNoteTest {
    private val ars = CurrencyCode("ARS")
    private fun money(v: Long) = Money.ofMajor(v, currency = ars)
    private val format: (Money) -> String = { "${it.amountMinor / 100} ARS" }
    private fun receipt(items: List<ReceiptItem>) = Receipt(money(82_400), LocalDate(2026, 11, 14), "Teleshka", "teleshka:4807", "teleshka", items)

    @Test
    fun `shop first, then one line per item`() {
        val note = ReceiptNote.of(receipt(listOf(ReceiptItem("Пельмени, 1кг", "1", money(25_000)), ReceiptItem("Джерки", "2", money(12_000)))), format)
        assertEquals("Teleshka\nПельмени, 1кг — 25000 ARS\nДжерки ×2 — 12000 ARS", note)
    }

    @Test
    fun `no items, no note`() {
        assertNull(ReceiptNote.of(receipt(emptyList()), format))
    }

    @Test
    fun `a long list is cut on a line with an ellipsis`() {
        val note = ReceiptNote.of(receipt(List(100) { ReceiptItem("Товар номер $it с длинным названием", "1", money(1_000)) }), format)!!
        assertTrue(note.length <= ReceiptNote.MAX)
        assertTrue(note.endsWith("\n…"))
    }
}
