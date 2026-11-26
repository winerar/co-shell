package app.coshell.core.domain.receipts

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EmvQrFormatTest {
    private val day = LocalDate(2026, 11, 26)
    private val format = EmvQrFormat { day }

    /** A dynamic Mercado Pago-like QR: 12 500,50 ARS, MCC 5812, «CAFE MARTINEZ». */
    private val dynamic = "00020101021226300015com.mercadopago01071234567520458125303032540812500.505802AR5913CAFE MARTINEZ6004CABA62120508REF000426304B093"
    private val static = "00020101021126300015com.mercadopago010712345675204581253030325802AR5913CAFE MARTINEZ6004CABA62120508REF0004263043085"
    private val amd = "00020101021226300015com.mercadopago01071234567520454115303051540812500.505802AR5915SAS SUPERMARKET6004CABA62120508REF000426304F83A"

    @Test
    fun `reads the sum, the shop and the category code`() {
        val receipt = format.read(dynamic)!!
        assertEquals(Money(1_250_050, CurrencyCode("ARS")), receipt.total)
        assertEquals("CAFE MARTINEZ", receipt.merchant)
        assertEquals("5812", receipt.mcc)
        assertEquals(day, receipt.date)
        assertTrue(receipt.key.startsWith("emv:"))
        assertEquals(receipt.key, format.read(dynamic)!!.key)
        assertNotEquals(receipt.key, format.read(amd)!!.key)
    }

    @Test
    fun `the currency comes from its numeric code`() {
        assertEquals(CurrencyCode("AMD"), format.read(amd)!!.total.currency)
    }

    @Test
    fun `a QR after the text layer of a PDF is found`() {
        assertEquals("CAFE MARTINEZ", format.read("Comprobante\nPagá con QR\n$dynamic")!!.merchant)
    }

    @Test
    fun `a shop QR without a sum, a wrong CRC or foreign text is not read`() {
        assertNull(format.read(static))
        assertNull(format.read(dynamic.dropLast(4) + "0000"))
        assertNull(format.read(dynamic.replace("12500.50", "99999.50")))
        assertNull(format.read("000201 hello"))
    }

    @Test
    fun `the CRC is the EMVCo one`() {
        assertEquals("29B1", Emv.crc16("123456789"))
    }
}
