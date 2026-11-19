package app.coshell.core.domain.quickentry

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalEncodingApi::class)
class AfipReceiptQrTest {
    private val ars = CurrencyCode("ARS")

    // A real supermarket receipt: 37 914 ARS, factura B.
    private val real = "https://www.afip.gob.ar/fe/qr/?p=eyJ2ZXIiOjEsImN0eiI6MSwidGlwb0RvY1JlYyI6OTksInB0b1Z0YSI6NDQyLCJpbXBvcnRlIjozNzkxNCwidGlwb0NtcCI6NiwiZmVjaGEiOiIyMDI2LTExLTE5IiwiY29kQXV0Ijo4NjQ3MzQ2MjgyODA0OSwiY3VpdCI6MzA2ODU4NDk3NTEsInRpcG9Db2RBdXQiOiJFIiwibnJvQ21wIjoxMTc4MTcsIm5yb0RvY1JlYyI6MCwibW9uZWRhIjoiQVJTIn0="

    private fun link(json: String) =
        "https://www.afip.gob.ar/fe/qr/?p=" + Base64.UrlSafe.encode(json.encodeToByteArray())

    private fun json(importe: String = "100", moneda: String = "\"ARS\"", ver: String = "1") =
        """{"ver":$ver,"fecha":"2026-11-19","cuit":30685849751,"ptoVta":442,"tipoCmp":6,"nroCmp":117817,"importe":$importe,"moneda":$moneda}"""

    @Test
    fun `real receipt gives amount date and key`() {
        val qr = AfipReceiptQr.find(real)!!
        assertEquals(Money.ofMajor(37_914, currency = ars), qr.money)
        assertEquals(LocalDate(2026, 11, 19), qr.date)
        assertEquals("30685849751:442:6:117817", qr.key)
        assertEquals("30-68584975-1", qr.sellerTaxIdText)
    }

    @Test
    fun `link inside a shared message and with percent-encoded padding`() {
        val text = "Mi ticket: " + real.removeSuffix("=") + "%3D gracias"
        assertEquals(Money.ofMajor(37_914, currency = ars), AfipReceiptQr.find(text)!!.money)
    }

    @Test
    fun `base64 without padding and the bare payload work`() {
        val noPad = real.trimEnd('=')
        assertEquals(37_914_00L, AfipReceiptQr.find(noPad)!!.money.amountMinor)
        assertEquals(37_914_00L, AfipReceiptQr.find(real.substringAfter("p="))!!.money.amountMinor)
    }

    @Test
    fun `decimal amounts are read exactly`() {
        assertEquals(Money.ofMajor(379, 14, ars), AfipReceiptQr.find(link(json("379.14")))!!.money)
        assertEquals(Money.ofMajor(379, 50, ars), AfipReceiptQr.find(link(json("379.5")))!!.money)
        // A dot is always the decimal separator here: «1.500» is one peso fifty, not a thousand five hundred.
        assertEquals(150L, AfipReceiptQr.find(link(json("1.500")))!!.money.amountMinor)
    }

    @Test
    fun `too many fraction digits is not trusted`() {
        assertNull(AfipReceiptQr.find(link(json("10.123"))))
    }

    @Test
    fun `dollar receipts map to USD`() {
        assertEquals(CurrencyCode("USD"), AfipReceiptQr.find(link(json("20", "\"DOL\"")))!!.money.currency)
    }

    @Test
    fun `refuses what is not a receipt`() {
        assertNull(AfipReceiptQr.find("Compra \$12.500,00 en COTO"))
        assertNull(AfipReceiptQr.find("https://evil.example/fe/qr/?p=" + real.substringAfter("p=")))
        assertNull(AfipReceiptQr.find(link(json(ver = "2"))))
        assertNull(AfipReceiptQr.find(link(json("0"))))
        assertNull(AfipReceiptQr.find(link(json("-5"))))
        assertNull(AfipReceiptQr.find(link(json("1e3"))))
        assertNull(AfipReceiptQr.find(link(json(moneda = "12"))))
        assertNull(AfipReceiptQr.find("https://www.afip.gob.ar/fe/qr/?p=!!!notbase64!!!"))
    }
}
