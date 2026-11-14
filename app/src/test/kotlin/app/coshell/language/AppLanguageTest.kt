package app.coshell.language

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `the phone language is used when nothing is chosen`() {
        assertEquals("es", AppLanguage.pick(null, listOf("es-AR")))
        assertEquals("ru", AppLanguage.pick(null, listOf("ru-RU", "en-US")))
    }

    @Test
    fun `the first supported phone language wins`() {
        assertEquals("es", AppLanguage.pick(null, listOf("fr-FR", "es-ES", "en-US")))
    }

    @Test
    fun `an unsupported phone falls back to English`() {
        assertEquals("en", AppLanguage.pick(null, listOf("fr-FR", "de")))
        assertEquals("en", AppLanguage.pick(null, emptyList()))
    }

    @Test
    fun `the choice in the app beats the phone`() {
        assertEquals("ru", AppLanguage.pick("ru", listOf("es-AR")))
    }

    @Test
    fun `an unknown saved choice is ignored`() {
        assertEquals("es", AppLanguage.pick("fr", listOf("es")))
    }

    @Test
    fun `the app stays Russian until the language picker ships`() {
        assertFalse(AppLanguage.FOLLOW_PHONE)
    }
}
