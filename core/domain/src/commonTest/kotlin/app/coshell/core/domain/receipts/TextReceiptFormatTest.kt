package app.coshell.core.domain.receipts

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextReceiptFormatTest {
    private val russian = """
        ООО «Ромашка»
        ИНН 7701234567
        КАССОВЫЙ ЧЕК 26.11.2026 15:30
        Хлеб 1 x 89,90
        Молоко 2 x 120,00
        Подытог 329,90
        ИТОГ =329,90
        Безналичными 329,90
    """.trimIndent()

    private val spanish = """
        CAFE MARTINEZ
        CUIT 30-68584975-1
        Fecha: 19/11/26
        Cafe con leche   4.500,00
        Medialunas       3.000,00
        TOTAL ITEMS 2
        SUBTOTAL 7.500,00
        TOTAL
        7.500,00
    """.trimIndent()

    @Test
    fun `a russian receipt by its total, tax id and date`() {
        val receipt = ReceiptFormats.read(russian)!!
        assertEquals("text", receipt.format)
        assertEquals(Money(32_990, CurrencyCode("RUB")), receipt.total)
        assertEquals(LocalDate(2026, 11, 26), receipt.date)
        assertEquals("ООО «Ромашка»", receipt.merchant)
        assertEquals(receipt.key, TextReceiptFormat.read(russian.replace("\n", "\n  "))!!.key)
    }

    @Test
    fun `the total on the next line, item counts and subtotals ignored`() {
        val receipt = TextReceiptFormat.read(spanish)!!
        assertEquals(Money(750_000, CurrencyCode("ARS")), receipt.total)
        assertEquals(LocalDate(2026, 11, 19), receipt.date)
        assertEquals("CAFE MARTINEZ", receipt.merchant)
    }

    @Test
    fun `the currency at the total wins over the tax id`() {
        val receipt = TextReceiptFormat.read(russian.replace("ИТОГ =329,90", "ИТОГ 329,90 USD"))!!
        assertEquals(CurrencyCode("USD"), receipt.total.currency)
    }

    @Test
    fun `without a tax id the language tells the currency`() {
        assertEquals(CurrencyCode("RUB"), TextReceiptFormat.read(russian.replace("ИНН 7701234567", "Касса 1"))!!.total.currency)
        assertEquals(CurrencyCode("ARS"), TextReceiptFormat.read(spanish.replace("CUIT 30-68584975-1", "Sucursal 12"))!!.total.currency)
    }

    @Test
    fun `a bill is dated by the day it was paid`() {
        val bill = "EDENOR\nFactura B 0098\nFecha de emisión: 05/11/2026\nTotal pagado: ${'$'} 38.412,77\nPagado el 16/11/2026"
        assertEquals(LocalDate(2026, 11, 16), TextReceiptFormat.read(bill)!!.date)
    }

    @Test
    fun `the code at the total wins over the dollar sign`() {
        val steam = "STEAM PURCHASE\nThank you\nDate issued: 08 Nov, 2026\nTotal: ${'$'}24.98 USD\nVisa"
        assertEquals(Money(2_498, CurrencyCode("USD")), TextReceiptFormat.read(steam)!!.total)
    }

    @Test
    fun `the largest number of a total line, not a date on it`() {
        val receipt = TextReceiptFormat.read(spanish.replace("TOTAL\n7.500,00", "TOTAL 7.500,00 19/11/26"))!!
        assertEquals(750_000, receipt.total.amountMinor)
    }

    @Test
    fun `no total, no date, no currency or a short message is not read`() {
        assertNull(TextReceiptFormat.read(russian.replace("ИТОГ =329,90", "Оплачено")))
        assertNull(TextReceiptFormat.read(russian.replace("26.11.2026", "")))
        assertNull(TextReceiptFormat.read("BLUE SHOP\nStore 12\n12/11/2026\nTOTAL 13.75\nThank you"))
        assertNull(TextReceiptFormat.read("Покупка 26.11.2026\nИтого 500 ₽"))
    }
}
