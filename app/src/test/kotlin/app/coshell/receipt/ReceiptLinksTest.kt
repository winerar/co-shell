package app.coshell.receipt

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReceiptLinksTest {
    private val link = "https://www.afip.gob.ar/fe/qr/?p=eyJ2ZXIiOjEsImN0eiI6MSwidGlwb0RvY1JlYyI6OTksInB0b1Z0YSI6NDQyLCJpbXBvcnRlIjozNzkxNCwidGlwb0NtcCI6NiwiZmVjaGEiOiIyMDI2LTExLTE5IiwiY29kQXV0Ijo4NjQ3MzQ2MjgyODA0OSwiY3VpdCI6MzA2ODU4NDk3NTEsInRpcG9Db2RBdXQiOiJFIiwibnJvQ21wIjoxMTc4MTcsIm5yb0RvY1JlYyI6MCwibW9uZWRhIjoiQVJTIn0="

    @Test
    fun `a view intent with a receipt link is taken`() {
        assertEquals(link, ReceiptLinks.textOf(Intent.ACTION_VIEW, link))
    }

    @Test
    fun `other links and other actions are left alone`() {
        assertNull(ReceiptLinks.textOf(Intent.ACTION_VIEW, "https://www.afip.gob.ar/fe/qr/?p=!!"))
        assertNull(ReceiptLinks.textOf(Intent.ACTION_VIEW, "https://example.com/"))
        assertNull(ReceiptLinks.textOf(Intent.ACTION_SEND, link))
        assertNull(ReceiptLinks.textOf(null, null))
    }
}
