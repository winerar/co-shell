package app.coshell.gate

import org.junit.Assert.assertEquals
import org.junit.Test

/** #334: while entry is closed, guarded actions show the note instead of writing. */
class InputLockTest {
    private var blocked = 0
    private val done = mutableListOf<String>()

    private fun lock(locked: Boolean) = InputLock(locked) { blocked++ }

    @Test
    fun `open lock runs the action`() {
        val lock = lock(locked = false)
        lock.guard { done += "save" }()
        lock.guard1<String> { done += it }("tag")
        lock.guard2<String, Int> { a, b -> done += "$a$b" }("x", 1)

        assertEquals(listOf("save", "tag", "x1"), done)
        assertEquals(0, blocked)
    }

    @Test
    fun `closed lock shows the note and writes nothing`() {
        val lock = lock(locked = true)
        lock.guard { done += "save" }()
        lock.guard1<String> { done += it }("tag")
        lock.guard2<String, Int> { a, b -> done += "$a$b" }("x", 1)

        assertEquals(emptyList<String>(), done)
        assertEquals(3, blocked)
    }
}
