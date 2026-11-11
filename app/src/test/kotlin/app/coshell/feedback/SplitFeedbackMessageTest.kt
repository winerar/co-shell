package app.coshell.feedback

import org.junit.Assert.assertEquals
import org.junit.Test

class SplitFeedbackMessageTest {
    @Test
    fun `kind in front is split off`() = assertEquals("Ошибка" to "шторка: дёргается", splitFeedbackMessage("Ошибка: шторка: дёргается"))

    @Test
    fun `text without a short kind stays whole`() {
        assertEquals(null to "просто текст", splitFeedbackMessage("просто текст"))
        val long = "Очень длинное начало без вида: дальше"
        assertEquals(null to long, splitFeedbackMessage(long))
    }
}
