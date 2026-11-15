package app.coshell.language

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageChoicesTest {
    @Test
    fun `settings and the first launch list the phone and then every language`() {
        assertEquals(listOf(null, "ru", "en", "es"), LanguageChoices.all().map { it.tag })
        assertEquals(LanguageChoices.all(), LanguageChoices.forPrompt(LanguagePrompt.FIRST))
    }

    @Test
    fun `after the update keeping Russian comes first and Russian is not listed twice`() {
        val choices = LanguageChoices.forPrompt(LanguagePrompt.UPDATE)
        assertEquals(LanguageChoice("ru", keep = true), choices.first())
        assertEquals(listOf("ru", null, "en", "es"), choices.map { it.tag })
    }
}
