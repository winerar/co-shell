package app.coshell.hints

import app.coshell.core.domain.hints.HintId
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** Hints stay in step with the docs and the languages (`docs/agents/hints.md` §«Где в коде»). */
class HintsCoverageTest {
    private val res = File("src/main/res")
    private val docs = File("../docs/agents/hints.md").readText()

    @Test
    fun `every hint id is listed in the hints doc`() {
        val missing = HintId.entries.filter { "`${it.key}`" !in docs }.map { it.key }
        assertTrue("Not in hints.md: $missing", missing.isEmpty())
    }

    @Test
    fun `every hint shown by a screen has text in every language`() {
        val used = File("src/main/kotlin").walkTopDown().filter { it.extension == "kt" && "/hints/" !in it.path }
            .flatMap { f -> Regex("""HintId\.([A-Z_0-9]+)""").findAll(f.readText()).map { it.groupValues[1] } }
            .toSet().mapNotNull { name -> HintId.entries.firstOrNull { it.name == name } }
        val missing = mutableListOf<String>()
        for (dir in listOf("values", "values-en", "values-es")) {
            val text = File(res, "$dir/strings_hints.xml").readText()
            used.filter { "name=\"hint_${it.key}\"" !in text }
                .forEach { missing += "$dir:${it.key}" }
        }
        assertTrue("No text for: $missing", missing.isEmpty())
    }
}
