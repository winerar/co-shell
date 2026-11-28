package app.coshell.core.domain.newmark

import kotlin.test.Test
import kotlin.test.assertEquals

class NewMarkTreeTest {
    private val inside = mapOf(
        "settings" to listOf("look", "screens"),
        "look" to listOf("density"),
        "screens" to listOf("bar"),
    )

    @Test
    fun `the mark leads from the menu to the new row`() {
        assertEquals(setOf("bar", "settings", "screens"), NewMarkTree.marked(setOf("bar"), inside))
    }

    @Test
    fun `a waypoint is marked while anything inside is`() {
        assertEquals(setOf("bar", "density", "settings", "look", "screens"), NewMarkTree.marked(setOf("bar", "density"), inside))
    }

    @Test
    fun `a waypoint has no mark of its own`() {
        assertEquals(emptySet(), NewMarkTree.marked(setOf("settings"), inside))
    }

    @Test
    fun `places outside the tree keep their marks`() {
        assertEquals(setOf("goals"), NewMarkTree.marked(setOf("goals"), inside))
    }

    @Test
    fun `a loop in the tree does not hang`() {
        assertEquals(emptySet(), NewMarkTree.marked(emptySet(), mapOf("a" to listOf("b"), "b" to listOf("a"))))
    }
}
