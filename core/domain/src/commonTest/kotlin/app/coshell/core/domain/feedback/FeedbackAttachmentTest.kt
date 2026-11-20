package app.coshell.core.domain.feedback

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeedbackAttachmentTest {
    private fun file(size: Int) = FeedbackAttachment("r.pdf", "application/pdf", ByteArray(size) { (it % 251).toByte() })

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `chunks glue back into the same bytes`() {
        val big = file(1_200_000)
        val chunks = big.chunks()
        assertEquals(3, chunks.size)
        assertTrue(chunks.all { it.length <= FeedbackAttachment.CHUNK_CHARS })
        assertContentEquals(big.bytes, Base64.Default.decode(chunks.joinToString("")))
    }

    @Test
    fun `limits on count and size`() {
        assertNull(FeedbackAttachment.problem(listOf(file(10), file(10), file(10))))
        assertEquals(FeedbackResult.TooManyFiles(3), FeedbackAttachment.problem(List(4) { file(1) }))
        assertEquals(FeedbackResult.FilesTooLarge(FeedbackAttachment.MAX_TOTAL_BYTES), FeedbackAttachment.problem(listOf(file(2_000_000), file(1_200_000))))
    }

    @Test
    fun `names lose paths and stay short`() {
        assertEquals("chek.pdf", FeedbackAttachment.cleanName("/sdcard/Download/chek.pdf"))
        assertEquals("file", FeedbackAttachment.cleanName("  "))
        assertEquals(120, FeedbackAttachment.cleanName("a".repeat(300)).length)
    }
}
