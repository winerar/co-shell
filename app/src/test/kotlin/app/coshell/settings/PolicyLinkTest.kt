package app.coshell.settings

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class PolicyLinkTest {
    @Test
    fun `builds the page in the app language`() {
        assertEquals("https://x.github.io/r/privacy/es/", PolicyLink.of("https://x.github.io/r/", "es"))
        assertEquals("https://co-shell.app/privacy/ru/", PolicyLink.of("https://co-shell.app", "ru"))
    }

    @Test
    fun `falls back to English for a language the site does not have`() {
        assertEquals("https://x.github.io/r/privacy/en/", PolicyLink.of("https://x.github.io/r", "de"))
    }

    @Test
    fun `no site means no link`() {
        assertNull(PolicyLink.of("", "ru"))
        assertNull(PolicyLink.of("  ", "ru"))
        assertNull(PolicyLink.of("http://insecure.example", "ru"))
    }

    @Test
    fun `site languages match the policy pages`() {
        val script = java.io.File("../scripts/privacy_pages.py").readText()
        assertEquals("LANGS = [\"ru\", \"en\", \"es\"]", Regex("LANGS = \\[.*]").find(script)?.value)
        assertEquals(listOf("ru", "en", "es"), PolicyLink.LANGUAGES)
    }
}
