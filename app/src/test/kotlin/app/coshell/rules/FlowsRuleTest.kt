package app.coshell.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Board «Воркфлоу» (`Flows`) follows the app (#466, workflow.md §«Доска — источник истины»): every `*Destination`
 * of `navigation/Destinations.kt` is in `docs/design/flows-routes.txt` — with the board that is a step of `Flows`
 * (table `ROWS` in `gen-flows-board.py`) or with `-` and a reason; every step leads to an existing board.
 */
class FlowsRuleTest {
    private val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
    private val destinations = File(root, "app/src/main/kotlin/app/coshell/navigation/Destinations.kt").readText()
        .let { text -> DESTINATION.findAll(text).map { it.groupValues[1] }.toSet() }
    private val steps = File(root, "scripts/agent/gen-flows-board.py").readText()
        .let { text -> STEP.findAll(text).map { it.groupValues[1] }.toSet() }
    private val routes: Map<String, List<String>> = File(root, "docs/design/flows-routes.txt").readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") }.map { it.split('\t') }.associate { it[0] to it.drop(1) }

    @Test
    fun everyRouteIsMapped() {
        assertEquals("Destinations.kt and flows-routes.txt differ (#466)", destinations.sorted(), routes.keys.sorted())
    }

    @Test
    fun aBoardOrAReason() {
        val bad = routes.filter { (_, v) -> v[0] != "-" && v[0] !in steps || v[0] == "-" && v.getOrNull(1).isNullOrBlank() }.keys
        assertTrue("No Flows step for the board (or no reason for «-»): ${bad.sorted()}", bad.isEmpty())
    }

    @Test
    fun everyStepHasABoard() {
        val missing = steps.filterNot { File(root, "docs/design/mockups/$it.dc.html").exists() }
        assertTrue("Flows steps without a board file: $missing", missing.isEmpty())
    }

    @Test
    fun theRuleReadsTheSources() {
        assertTrue(destinations.size > 20 && steps.size > 20)
    }

    private companion object {
        val DESTINATION = Regex("""^data (?:object|class) (\w+Destination)\b""", RegexOption.MULTILINE)
        val STEP = Regex(""""(\w+)\.dc\.html"""")
    }
}
