package app.coshell.receipt

import android.content.Intent
import app.coshell.receipt.inbox.Incoming
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReceiptLinksTest {
    private val link = "https://www.afip.gob.ar/fe/qr/?p=eyJ2ZXIiOjEsImN0eiI6MSwidGlwb0RvY1JlYyI6OTksInB0b1Z0YSI6NDQyLCJpbXBvcnRlIjozNzkxNCwidGlwb0NtcCI6NiwiZmVjaGEiOiIyMDI2LTExLTE5IiwiY29kQXV0Ijo4NjQ3MzQ2MjgyODA0OSwiY3VpdCI6MzA2ODU4NDk3NTEsInRpcG9Db2RBdXQiOiJFIiwibnJvQ21wIjoxMTc4MTcsIm5yb0RvY1JlYyI6MCwibW9uZWRhIjoiQVJTIn0="

    private fun of(action: String?, type: String? = null, data: String? = null, text: String? = null, stream: String? = null) =
        Incoming.of(action, type, data, text, stream)

    @Test
    fun `a view intent with a receipt link is taken`() {
        assertEquals(Incoming.Text(link), of(Intent.ACTION_VIEW, data = link))
    }

    @Test
    fun `other links and other actions are left alone`() {
        assertNull(of(Intent.ACTION_VIEW, data = "https://www.afip.gob.ar/fe/qr/?p=!!"))
        assertNull(of(Intent.ACTION_VIEW, data = "https://example.com/"))
        assertNull(of(Intent.ACTION_MAIN, data = link))
        assertNull(of(null))
    }

    @Test
    fun `shared text is taken whatever it says`() {
        assertEquals(Incoming.Text("Compra 12.500"), of(Intent.ACTION_SEND, "text/plain", text = "Compra 12.500"))
        assertNull(of(Intent.ACTION_SEND, "text/plain", text = "  "))
    }

    @Test
    fun `shared pdf and images are files`() {
        assertEquals(Incoming.File("content://x/1", "application/pdf"), of(Intent.ACTION_SEND, "application/pdf", stream = "content://x/1"))
        assertEquals(Incoming.File("content://x/2", "image/jpeg"), of(Intent.ACTION_SEND, "image/jpeg", stream = "content://x/2", text = "caption"))
    }

    @Test
    fun `a file of another type falls back to the shared text`() {
        assertEquals(Incoming.Text("see"), of(Intent.ACTION_SEND, "application/zip", stream = "content://x/3", text = "see"))
    }
}
