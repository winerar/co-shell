package app.coshell.core.designsystem.amount

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmountWithCodeTest {
    private val grey = SpanStyle(color = Color.Gray)

    @Test
    fun codeAfterTheNumberIsStyled() {
        val text = "−1 234,50 USD"
        val result = amountWithCode(text, grey)
        assertEquals(text, result.text)
        val span = result.spanStyles.single()
        assertEquals("USD", text.substring(span.start, span.end))
        assertEquals(grey, span.item)
    }

    @Test
    fun symbolBeforeTheNumberIsLeftAlone() {
        assertTrue(amountWithCode("$1 234,50", grey).spanStyles.isEmpty())
    }

    @Test
    fun noCodeOrDigitsAfterTheSpaceIsLeftAlone() {
        assertTrue(amountWithCode("1 234", grey).spanStyles.isEmpty())
        assertTrue(amountWithCode("abc 12", grey).spanStyles.isEmpty())
        assertTrue(amountWithCode("12 ", grey).spanStyles.isEmpty())
    }
}
