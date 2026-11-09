package app.coshell.designsystem

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Keeps size changes one system (issue #64, motion.md): screens fold, unfold and resize only through
 * `core:designsystem` — `CoShellExpandable`, `Modifier.smoothResize()`, `listItemMotion()` — so every block
 * moves with the same `Motion.Resize` spring. A raw `AnimatedVisibility`, `animateContentSize` or
 * `animateItem` would bring its own timing — this test names the file to fix.
 */
class MotionRuleTest {
    private val forbidden = listOf(
        Regex("""^import androidx\.compose\.animation\.(AnimatedVisibility|animateContentSize|expandVertically|shrinkVertically|expandHorizontally|shrinkHorizontally)$""", RegexOption.MULTILINE),
        Regex("""\banimateItem\("""),
    )

    @Test
    fun `screens resize only through design system motion`() {
        val root = listOf(File("src/main/kotlin"), File("app/src/main/kotlin")).first { it.isDirectory }
        val offenders = root.walkTopDown()
            .filter { it.extension == "kt" }
            .flatMap { file ->
                val text = file.readText()
                forbidden.flatMap { rule -> rule.findAll(text).map { "${file.relativeTo(root)}: ${it.value}" }.toList() }
            }
            .toList()
        assertTrue("Use CoShellExpandable / smoothResize / listItemMotion instead:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
