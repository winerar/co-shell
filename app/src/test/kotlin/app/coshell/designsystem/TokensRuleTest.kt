package app.coshell.designsystem

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Screens use only design tokens (design-system.md §«Токены — правила»): `Spacing.*` / `Sizes.*` instead of
 * `16.dp`, `Motion.*` instead of `tween(…)`, theme colours instead of `Color(0x…)`. All screens were moved
 * on 2026-11-09; this test names the line to fix if a raw number creeps back.
 */
class TokensRuleTest {

    private val forbidden = listOf(
        Regex("""\b\d+(\.\d+)?\.dp\b"""),
        Regex("""\btween\("""),
        Regex("""\bspring\("""),
        Regex("""Color\(0x"""),
    )

    @Test
    fun `screens use only design tokens`() {
        val root = listOf(File("src/main/kotlin/app/coshell"), File("app/src/main/kotlin/app/coshell")).first { it.isDirectory }
        val offenders = root.walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
            file.readLines().mapIndexedNotNull { i, line ->
                if (forbidden.any { it.containsMatchIn(line) }) "${file.relativeTo(root)}:${i + 1}: ${line.trim()}" else null
            }
        }.toList()
        assertTrue("Use Spacing / Motion / theme colours instead:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
