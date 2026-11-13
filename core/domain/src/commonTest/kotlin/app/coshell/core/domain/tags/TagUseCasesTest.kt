package app.coshell.core.domain.tags

import app.coshell.core.domain.FakeTagRepository
import app.coshell.core.domain.tags.SaveTagUseCase.Reason
import app.coshell.core.domain.tags.SaveTagUseCase.Result
import app.coshell.core.model.Tag
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class TagUseCasesTest {
    private val repo = FakeTagRepository()
    private var n = 0
    private val save = SaveTagUseCase(repo) { "rnd${n++}" }
    private val ensure = EnsureLabelTagsUseCase(repo)

    private fun use(label: String, count: Int = 1, day: Long = 0) = TagUsage(label, count, Instant.fromEpochMilliseconds(day))

    @Test
    fun `new tag takes the id of its name so imported labels show it`() = runTest {
        assertEquals(Result.Saved(Tag("отпуск", "Отпуск", 0xFF00E5C0)), save(null, "  Отпуск ", 0xFF00E5C0))
    }

    @Test
    fun `taken id - even by a deleted tag - gives a random one`() = runTest {
        save(null, "Аня", null)
        save("аня", "Анна", null)
        assertEquals(Result.Saved(Tag("rnd0", "Аня", null)), save(null, "Аня", null))
        repo.deleteTag("rnd0")
        assertEquals("rnd1", (save(null, "аня", null) as Result.Saved).tag.id)
    }

    @Test
    fun `names are unique among live tags ignoring case and spaces`() = runTest {
        save(null, "Mercado Libre", null)
        assertEquals(Result.Invalid(Reason.DUPLICATE_NAME), save(null, "mercado  libre", null))
        // Renaming a tag to its own name in another case is fine.
        assertEquals(Result.Saved(Tag("mercado libre", "MERCADO LIBRE", 1)), save("mercado libre", "MERCADO LIBRE", 1))
        repo.deleteTag("mercado libre")
        assertEquals("rnd0", (save(null, "Mercado Libre", null) as Result.Saved).tag.id)
    }

    @Test
    fun `bad names and unknown ids are refused`() = runTest {
        assertEquals(Result.Invalid(Reason.BLANK_NAME), save(null, "   ", null))
        assertEquals(Result.Invalid(Reason.NAME_TOO_LONG), save(null, "a".repeat(Tag.MAX_NAME + 1), null))
        assertEquals(Result.Invalid(Reason.NOT_FOUND), save("nope", "x", null))
    }

    @Test
    fun `labels without tags become tags named as written`() = runTest {
        repo.saveTag(Tag("аня", "Анна"))
        repo.saveTag(Tag("кот", "кот")); repo.deleteTag("кот")
        repo.usage.value = mapOf(
            "mercado libre" to use("Mercado  Libre"),
            "аня" to use("Аня"),
            "кот" to use("кот"),
            "3f2a9c1e-0b4d-4e8a-9f1c-2d3e4f5a6b7c" to use("3f2a9c1e-0b4d-4e8a-9f1c-2d3e4f5a6b7c"),
        )
        assertEquals(1, ensure())
        assertEquals(listOf(Tag("mercado libre", "Mercado Libre"), Tag("аня", "Анна")), repo.live.sortedBy { it.name })
        assertEquals(0, ensure())
    }

    @Test
    fun `recent and frequent orders`() {
        val tags = listOf(Tag("a", "a"), Tag("b", "B"), Tag("c", "c"), Tag("d", "d"))
        val usage = mapOf("a" to use("a", 5, day = 10), "b" to use("b", 1, day = 30), "c" to use("c", 9, day = 10))
        assertEquals(listOf("b", "c"), TagRanking.recent(tags, usage, limit = 2).map { it.id })
        assertEquals(listOf("c", "a", "b", "d"), TagRanking.frequent(tags, usage).map { it.id })
    }

    @Test
    fun `search puts names starting with the query first`() {
        val tags = listOf(Tag("1", "такси ночью"), Tag("2", "Ночь"), Tag("3", "кот"))
        assertEquals(listOf("2", "1"), TagRanking.search(tags, " НОЧ").map { it.id })
        assertEquals(tags, TagRanking.search(tags, " "))
    }
}
