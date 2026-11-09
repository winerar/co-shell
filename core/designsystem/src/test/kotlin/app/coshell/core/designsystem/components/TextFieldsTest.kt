package app.coshell.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextFieldsTest {
    @Test
    fun `error replaces the hint under a field`() {
        assertEquals("Неверный код", supportingLine(error = "Неверный код", hint = "Три буквы"))
    }

    @Test
    fun `hint shows when there is no error`() {
        assertEquals("Три буквы", supportingLine(error = null, hint = "Три буквы"))
    }

    @Test
    fun `no error and no hint leave no line`() {
        assertNull(supportingLine(error = null, hint = null))
    }
}
