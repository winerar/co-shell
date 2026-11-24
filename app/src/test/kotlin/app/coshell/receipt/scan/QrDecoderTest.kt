package app.coshell.receipt.scan

import com.google.zxing.BarcodeFormat
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.qrcode.QRCodeWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrDecoderTest {
    private val link = "https://www.afip.gob.ar/fe/qr/?p=eyJ2ZXIiOjEsImZlY2hhIjoiMjAyNi0xMS0yNCIsImN1aXQiOjMwNjg1ODQ5NzUxfQ=="

    @Test
    fun `reads a dark QR on a light receipt`() {
        assertEquals(link, QrDecoder().decode(picture(link, inverted = false)))
    }

    @Test
    fun `reads a light QR on a dark screen`() {
        assertEquals(link, QrDecoder().decode(picture(link, inverted = true)))
    }

    @Test
    fun `reads a faint grey QR on a grey receipt`() {
        assertEquals(link, QrDecoder().decode(picture(link, inverted = false, dark = 0x70, light = 0xA0)))
    }

    @Test
    fun `gives nothing for a picture without a QR`() {
        val blank = IntArray(SIDE * SIDE) { 0xFFFFFFFF.toInt() }
        assertNull(QrDecoder().decode(RGBLuminanceSource(SIDE, SIDE, blank)))
    }

    private fun picture(text: String, inverted: Boolean, dark: Int = 0x00, light: Int = 0xFF): RGBLuminanceSource {
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, SIDE, SIDE)
        val pixels = IntArray(SIDE * SIDE) { i ->
            val on = matrix[i % SIDE, i / SIDE] != inverted
            val v = if (on) dark else light
            (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
        return RGBLuminanceSource(SIDE, SIDE, pixels)
    }

    private companion object {
        const val SIDE = 600
    }
}
