package app.coshell.transactions.edit

import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.domain.tags.TagUsage
import app.coshell.core.model.Tag
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class EntryTagsTest {
    private val day = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun `choices are sorted by name with counts and a colour picked from the id when none is set`() {
        val choices = tagChoices(
            listOf(Tag("b", "бассейн"), Tag("a", "Аня", 7L)),
            mapOf("b" to TagUsage("бассейн", 2, day)),
        )
        assertEquals(listOf("Аня", "бассейн"), choices.options.map { it.name })
        assertEquals(listOf(7L, CategoryPalette.forKey("b")), choices.options.map { it.color })
        assertEquals(listOf(0, 2), choices.options.map { it.count })
        assertEquals(listOf("b"), choices.recentIds)
    }

    @Test
    fun `labels keep their original form and new tags go by id`() {
        assertEquals(listOf("Mercado Libre", "аня"), mergeLabels(listOf("Mercado Libre", "Отпуск"), listOf("mercado libre", "аня")))
        assertEquals(emptyList<String>(), mergeLabels(listOf("Аня"), emptyList()))
        assertEquals(listOf("Аня"), mergeLabels(listOf("Аня", "аня "), listOf("аня")))
    }

    @Test
    fun `search finds by part of the name, starting matches first`() {
        val options = listOf(TagOption("1", "бассейн", 0), TagOption("2", "Аня", 0), TagOption("3", "сейф", 0))
        assertEquals(listOf("3", "1"), searchTags(options, "сей").map { it.id })
        assertEquals(options, searchTags(options, "  "))
    }
}
