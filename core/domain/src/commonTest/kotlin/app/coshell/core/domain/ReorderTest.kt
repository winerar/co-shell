package app.coshell.core.domain

import app.coshell.core.domain.Reorder.Item
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ReorderTest {
    private fun items(vararg orders: Int) = orders.mapIndexed { i, o -> Item("a$i", o) }

    /** Applies the result and returns ids in the new display order. */
    private fun apply(shown: List<Item>, changes: Map<String, Int>): List<String> =
        shown.map { it.copy(order = changes[it.id] ?: it.order) }.sortedBy { it.order }.map { it.id }

    @Test
    fun `gapped list changes only the moved row`() {
        val shown = items(1024, 2048, 3072, 4096)
        val changes = Reorder.move(shown, from = 3, to = 1)
        assertEquals(mapOf("a3" to 1536), changes)
        assertEquals(listOf("a0", "a3", "a1", "a2"), apply(shown, changes))
    }

    @Test
    fun `moves to the very top and bottom`() {
        val shown = items(1024, 2048, 3072)
        assertEquals(mapOf("a2" to 0), Reorder.move(shown, 2, 0))
        assertEquals(mapOf("a0" to 3072 + 1024), Reorder.move(shown, 0, 2))
    }

    @Test
    fun `dense legacy orders are renumbered with gaps`() {
        val shown = items(0, 1, 2, 3)
        val changes = Reorder.move(shown, 0, 2)
        assertEquals(listOf("a1", "a2", "a0", "a3"), apply(shown, changes))
        assertTrue(changes.values.all { it % Reorder.STEP == 0 })
    }

    @Test
    fun `equal orders (never sorted) are renumbered`() {
        val shown = items(0, 0, 0)
        val changes = Reorder.move(shown, 2, 1)
        assertEquals(listOf("a0", "a2", "a1"), apply(shown, changes))
    }

    @Test
    fun `same place is a no-op`() {
        assertEquals(emptyMap(), Reorder.move(items(1, 2), 1, 1))
    }

    @Test
    fun `last goes after the biggest`() {
        assertEquals(5 + Reorder.STEP, Reorder.last(items(5, 3)))
        assertEquals(Reorder.STEP, Reorder.last(emptyList()))
    }

    @Test
    fun `bad index fails`() {
        assertFailsWith<IllegalArgumentException> { Reorder.move(items(1), 0, 1) }
    }
}
