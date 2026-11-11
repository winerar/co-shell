package app.coshell.core.domain.templates

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateUsageTest {
    private val ids = listOf("a", "b", "c", "d")
    private val counts = mapOf("c" to 4, "b" to 1, "gone" to 9)

    @Test
    fun `most used first, the unused keep their order`() {
        assertEquals(listOf("c", "b", "a", "d"), ids.byUsage(counts) { it })
        assertEquals(ids, ids.byUsage(emptyMap()) { it })
    }

    @Test
    fun `frequent leaves out the never used and respects the limit`() {
        assertEquals(listOf("c", "b"), ids.frequent(counts, limit = 3) { it })
        assertEquals(listOf("c"), ids.frequent(counts, limit = 1) { it })
        assertEquals(emptyList(), ids.frequent(emptyMap(), limit = 3) { it })
    }
}
