package app.coshell.core.domain.receipts

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Dates as receipts print them (#742): numbers day first, ISO, month words in Russian, Spanish, English. */
class ReceiptDatesTest {
    private val nov19 = LocalDate(2026, 11, 19)

    @Test
    fun `numeric dates are day first, with a short or full year`() {
        assertEquals(listOf(nov19), ReceiptDates.find("Fecha 19/11/26 Hora 18:55"))
        assertEquals(listOf(LocalDate(2026, 11, 15)), ReceiptDates.find("Дата 15.11.2026 23:10"))
        assertEquals(listOf(LocalDate(2026, 11, 12)), ReceiptDates.find("2026-11-12 08:15"))
    }

    @Test
    fun `month words in three languages`() {
        assertEquals(LocalDate(2026, 11, 14), ReceiptDates.parse("Viernes, 14 de noviembre de 2026 a las 21:03"))
        assertEquals(LocalDate(2026, 11, 10), ReceiptDates.parse("Поездка 10 ноября 2026 в 08:45"))
        assertEquals(LocalDate(2026, 11, 8), ReceiptDates.parse("Date issued: 08 Nov, 2026 @ 3:12pm"))
        assertEquals(LocalDate(2026, 11, 15), ReceiptDates.parse("15 Nov 2026, 22:47"))
        assertEquals(LocalDate(2026, 11, 15), ReceiptDates.parse("Order placed Nov 15, 2026"))
    }

    @Test
    fun `amounts, phone numbers and ids are not dates, and dates come in printed order`() {
        assertEquals(emptyList(), ReceiptDates.find("TOTAL $ 11.997,00 CUIT 30-54808315-6 Nro 0112-00458812 4.250,00"))
        assertNull(ReceiptDates.parse("null"))
        assertEquals(
            listOf(LocalDate(2026, 11, 5), LocalDate(2026, 11, 18), LocalDate(2026, 11, 16)),
            ReceiptDates.find("emisión: 05/11/2026\n1er Vencimiento: 18/11/2026\nPagado el 16/11/2026\nVencimiento 18/11/2026"),
        )
    }
}
