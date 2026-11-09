package app.coshell.designsystem

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Screens moved onto the design system (design-system.md §«Порядок переезда», step 4) keep only tokens:
 * `Spacing.*` instead of `16.dp`, `Motion.*` instead of `tween(…)`. Add a package here when its screen
 * is moved; this test names the line to fix if a raw number creeps back.
 */
class TokensRuleTest {
    private val migrated = listOf("home", "transactions/edit", "transactions/list", "ui/form", "ui/sheet", "ui/top", "undo")

    private val forbidden = listOf(
        Regex("""\b\d+(\.\d+)?\.dp\b"""),
        Regex("""\btween\("""),
        Regex("""\bspring\("""),
        Regex("""Color\(0x"""),
    )

    @Test
    fun `moved screens use only design tokens`() {
        val root = listOf(File("src/main/kotlin/app/coshell"), File("app/src/main/kotlin/app/coshell")).first { it.isDirectory }
        val offenders = migrated.flatMap { pkg ->
            File(root, pkg).walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
                file.readLines().mapIndexedNotNull { i, line ->
                    if (forbidden.any { it.containsMatchIn(line) }) "${file.relativeTo(root)}:${i + 1}: ${line.trim()}" else null
                }
            }.toList()
        }
        assertTrue("Use Spacing / Motion / theme colours instead:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
