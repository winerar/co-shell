package app.coshell.core.domain.receipts

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FnsQrFormatTest {
    private val rub = CurrencyCode("RUB")
    private val qr = "t=20261126T1530&s=1234.56&fn=7380440700076549&i=12345&fp=1234567890&n=1"

    @Test
    fun `reads the sum, the day and the key`() {
        val receipt = ReceiptFormats.read(qr)!!
        assertEquals("fns-qr", receipt.format)
        assertEquals(Money(123_456, rub), receipt.total)
        assertEquals(LocalDate(2026, 11, 26), receipt.date)
        assertEquals("fns:7380440700076549:12345:1234567890", receipt.key)
        assertEquals("", receipt.merchant)
        assertFalse(receipt.refund)
    }

    @Test
    fun `fields in any order, seconds, whole rubles`() {
        val receipt = FnsQrFormat.read("fn=9999078900004321&fp=3326107473&i=7&n=1&s=990&t=20261103T091502")!!
        assertEquals(Money(99_000, rub), receipt.total)
        assertEquals(LocalDate(2026, 11, 3), receipt.date)
    }

    @Test
    fun `a refund QR is a refund`() {
        assertTrue(FnsQrFormat.read(qr.replace("n=1", "n=2"))!!.refund)
    }

    @Test
    fun `the shop paying the person or a broken QR is not read`() {
        assertNull(FnsQrFormat.read(qr.replace("n=1", "n=3")))
        assertNull(FnsQrFormat.read(qr.replace("s=1234.56", "s=0")))
        assertNull(FnsQrFormat.read(qr.replace("s=1234.56", "s=12.345")))
        assertNull(FnsQrFormat.read(qr.replace("&fp=1234567890", "")))
        assertNull(FnsQrFormat.read(qr.replace("t=20261126T1530", "t=20261340T1530")))
        assertNull(FnsQrFormat.read("Покупка 1 234,56 ₽"))
    }
}
