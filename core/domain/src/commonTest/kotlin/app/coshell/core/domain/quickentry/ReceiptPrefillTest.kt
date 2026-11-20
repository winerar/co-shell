package app.coshell.core.domain.quickentry

import app.coshell.core.domain.receipts.toReceipt
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class ReceiptPrefillTest {
    private val ars = CurrencyCode("ARS")
    private val usdt = CurrencyCode("USDT")
    private val qr = ReceiptQr(Money.ofMajor(37_914, currency = ars), LocalDate(2026, 11, 19), "30685849751", 442, 6, 117_817).toReceipt()

    @Test
    fun `account in the receipt currency gets the plain amount`() {
        val prefill = ReceiptPrefills.of(qr, ars)
        assertEquals(qr.total, prefill.amount)
        assertNull(prefill.purchase)
        assertEquals(LocalDate(2026, 11, 19), prefill.date)
        assertEquals("30685849751:442:6:117817", prefill.dedupeKey)
    }

    @Test
    fun `no account chosen yet fills the plain amount`() {
        assertEquals(qr.total, ReceiptPrefills.of(qr, null).amount)
    }

    @Test
    fun `account in another currency gets the purchase amount and no debited sum`() {
        val prefill = ReceiptPrefills.of(qr, usdt)
        assertNull(prefill.amount)
        assertEquals(qr.total, prefill.purchase)
    }
}
