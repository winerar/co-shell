package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class TagTest {
    @Test
    fun `reference resolves to a trimmed lower-case id without slashes`() {
        assertEquals("такси ночью", TagIds.of("  Такси   НОЧЬЮ "))
        assertEquals("a|b", TagIds.of("A/B"))
        assertEquals("3f2a-uuid", TagIds.of("3f2a-uuid"))
    }

    @Test
    fun `labels resolve without blanks and repeats`() {
        assertEquals(listOf("аня", "отпуск"), TagIds.resolve(listOf("Аня", " ", "отпуск", "аня ")))
    }

    @Test
    fun `random ids are told from names`() {
        assertEquals(true, TagIds.isGenerated(IdGenerator.Random.newId()))
        assertEquals(false, TagIds.isGenerated("отпуск"))
    }

    @Test
    fun `clean name keeps case`() {
        assertEquals("Mercado Libre", TagIds.cleanName("  Mercado   Libre "))
    }

    @Test
    fun `operation tags come from its labels`() {
        val t = Transaction(
            "t", TransactionType.EXPENSE, "galicia", Money(100, CurrencyCode("ARS")), Instant.fromEpochMilliseconds(0),
            labels = listOf("Испанский", "Аня"),
        )
        assertEquals(listOf("испанский", "аня"), t.tagIds)
    }

    @Test
    fun `name is checked`() {
        assertFailsWith<IllegalArgumentException> { Tag("x", " ") }
        assertFailsWith<IllegalArgumentException> { Tag("x", "a".repeat(Tag.MAX_NAME + 1)) }
        assertFailsWith<IllegalArgumentException> { Tag("a/b", "a") }
    }
}
