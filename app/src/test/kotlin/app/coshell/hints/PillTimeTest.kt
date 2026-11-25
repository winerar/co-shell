package app.coshell.hints

import org.junit.Assert.assertEquals
import org.junit.Test

/** A warm pill stays long enough to read (onboarding check 2026-11-25). */
class PillTimeTest {
    @Test
    fun `short pills stay 8 seconds`() {
        assertEquals(8_000L, pillMillis("Подсказки включены"))
    }

    @Test
    fun `long pills get more time to read`() {
        val backup = "Записей уже много. Сохраните резервную копию, чтобы ничего не потерять: Ещё → Резервная копия."
        assertEquals(4_000L + 70L * backup.length, pillMillis(backup))
    }

    @Test
    fun `never longer than 15 seconds`() {
        assertEquals(15_000L, pillMillis("x".repeat(500)))
    }
}
