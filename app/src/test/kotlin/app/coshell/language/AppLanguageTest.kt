package app.coshell.language

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `app speaks Russian when no language is chosen`() {
        assertEquals(Locale.forLanguageTag("ru"), AppLanguage.resolve(null))
    }

    @Test
    fun `a language the app has no strings for falls back to Russian`() {
        assertEquals(Locale.forLanguageTag("ru"), AppLanguage.resolve("en"))
    }

    @Test
    fun `a supported language is used as is`() {
        assertEquals(Locale.forLanguageTag("ru"), AppLanguage.resolve("ru"))
    }
}
