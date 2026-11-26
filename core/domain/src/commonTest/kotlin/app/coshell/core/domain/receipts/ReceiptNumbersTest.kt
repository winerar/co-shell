package app.coshell.core.domain.receipts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Sums as receipts print them (#742): which mark is the decimal one, and the «TOTAL» line. */
class ReceiptNumbersTest {
    private fun minor(token: String, digits: Int = 2) = ReceiptNumbers.read(token)?.minor(digits)

    @Test
    fun `the decimal mark is told from thousands`() {
        assertEquals(1_199_700, minor("11.997,00"))
        assertEquals(6_545_000, minor("65.450"))
        assertEquals(350_000, minor("3,500"))
        assertEquals(2_498, minor("24.98"))
        assertEquals(184_000, minor("1 840,00"))
        assertEquals(98_340_000, minor("983.400"))
        assertEquals(38_412_77, minor("38.412,77"))
        assertEquals(1_075_050, minor("1,075,050") ?.let { it / 100 })
        assertNull(minor("1.12345", 2))
    }

    @Test
    fun `the total line is the last one with a total word, not the subtotal`() {
        val text = "SUBTOTAL 13.330,00\nDTO. -1.333,00\nTOTAL $ 11.997,00\nSU VUELTO 0,00"
        assertEquals(1_199_700, ReceiptNumbers.byKeyword(text)?.minor(2))
        assertEquals(45_388, ReceiptNumbers.byKeyword("ИТОГ =453.88\nБЕЗНАЛИЧНЫМИ =453.88")?.minor(2))
        assertEquals(6_545_000, ReceiptNumbers.byKeyword("TOTAL 59.500\nTotal con propina 65.450")?.minor(2))
        assertNull(ReceiptNumbers.byKeyword("Gracias por su compra"))
    }
}
