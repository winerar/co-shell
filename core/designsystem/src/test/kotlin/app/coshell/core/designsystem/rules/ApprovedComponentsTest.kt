package app.coshell.core.designsystem.rules

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Owner's rule (2026-11-15, #367): the app is built only from the approved set of components.
 * Approved = drawn on the «Компоненты» boards (`scripts/agent/mockup_catalog*.py`, `docs/design/`).
 * A new component: mock-up → owner's «ок» → board → code (`docs/agents/components.md` §«Утверждённый набор»).
 *
 * [pending] — components that existed before the rule and were not on the board; emptied by the poll «Компоненты v3»
 * (#367, 2026-11-15). Never add a name here.
 */
class ApprovedComponentsTest {
    private val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
    private val sources = File(root, "core/designsystem/src/main/kotlin/app/coshell/core/designsystem")
    private val board = File(root, "scripts/agent").listFiles { f -> f.name.startsWith("mockup_catalog") && f.extension == "py" }!!
        .sortedBy { it.name }.joinToString("\n") { it.readText() }

    /** Public `@Composable fun Name(` of the design system, without the theme itself and the catalog screens. */
    private val components: Set<String> = sources.walkTopDown()
        .filter { it.extension == "kt" && "catalog" !in it.path.split(File.separatorChar) }
        .flatMap { file -> COMPOSABLE.findAll(file.readText()).map { it.groupValues[1] } }
        .filterNot { it == "CoShellTheme" }
        .toSet()

    private fun onBoard(name: String) = Regex("\\b$name\\b").containsMatchIn(board)

    @Test
    fun everyComponentIsOnTheBoardOrWaitsForApproval() {
        val unapproved = components.filterNot { onBoard(it) || it in pending }.sorted()
        assertTrue(
            "Not on the «Компоненты» boards: $unapproved — mock-up → owner's «ок» → board (mockup_catalog*.py) → code (#367)",
            unapproved.isEmpty(),
        )
    }

    @Test
    fun approvedComponentsLeaveThePendingList() {
        val done = pending.filter { onBoard(it) || it !in components }.sorted()
        assertTrue("On the board or removed — delete from `pending`: $done", done.isEmpty())
    }

    @Test
    fun theScanFindsComponents() = assertTrue("scan found ${components.size}", components.size > 30 && "CoShellButton" in components)

    private companion object {
        val COMPOSABLE = Regex("""@Composable\s+(?:@\w+(?:\([^)]*\))?\s+)*fun\s+([A-Z]\w*)\s*\(""")

        /** Before the rule, not on the board (#367). Empty since 2026-11-15; never add. */
        val pending = emptySet<String>()
    }
}
