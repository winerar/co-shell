package app.coshell.core.domain.receipts

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReceiptFormatsTest {
    private val afip = "https://www.afip.gob.ar/fe/qr/?p=eyJ2ZXIiOjEsImN0eiI6MSwidGlwb0RvY1JlYyI6OTksInB0b1Z0YSI6NDQyLCJpbXBvcnRlIjozNzkxNCwidGlwb0NtcCI6NiwiZmVjaGEiOiIyMDI2LTExLTE5IiwiY29kQXV0Ijo4NjQ3MzQ2MjgyODA0OSwiY3VpdCI6MzA2ODU4NDk3NTEsInRpcG9Db2RBdXQiOiJFIiwibnJvQ21wIjoxMTc4MTcsIm5yb0RvY1JlYyI6MCwibW9uZWRhIjoiQVJTIn0="

    @Test
    fun `the receipt QR keeps its saved key`() {
        val receipt = ReceiptFormats.read("Mirá: $afip")!!
        assertEquals("afip-qr", receipt.format)
        assertEquals("30685849751:442:6:117817", receipt.key)
        assertEquals("30-68584975-1", receipt.merchant)
        assertEquals(Money.ofMajor(37_914, currency = CurrencyCode("ARS")), receipt.total)
    }

    @Test
    fun `ids are unique`() {
        assertEquals(ReceiptFormats.all.size, ReceiptFormats.all.map { it.id }.toSet().size)
    }

    @Test
    fun `plain text is no receipt`() {
        assertNull(ReceiptFormats.read("Compra con tarjeta ${"$"}12.500,00 en COTO"))
        assertNull(ReceiptFormats.read(""))
    }

    @Test
    fun `a huge text is read only from its head`() {
        assertNull(ReceiptFormats.read("x".repeat(ReceiptFormats.MAX_TEXT) + afip))
    }
}
