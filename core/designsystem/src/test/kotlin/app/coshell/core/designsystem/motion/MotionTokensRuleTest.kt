package app.coshell.core.designsystem.motion

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * One tempo for the whole app (owner 2026-11-09): inside the design system, animation specs come only from
 * `tokens/Motion.kt` — a raw `tween(…)`, `spring(…)` or easing curve elsewhere brings its own speed.
 * The app module has the same rule in `TokensRuleTest`.
 */
class MotionTokensRuleTest {
    private val forbidden = listOf(Regex("""\btween\("""), Regex("""\bspring\("""), Regex("""\bspring<"""), Regex("""CubicBezierEasing"""), Regex("""\bkeyframes\b"""))

    @Test
    fun `animation specs come only from Motion tokens`() {
        val root = listOf(File("src/main/kotlin"), File("core/designsystem/src/main/kotlin")).first { it.isDirectory }
        val offenders = root.walkTopDown()
            .filter { it.extension == "kt" && !it.path.endsWith("tokens/Motion.kt") }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { i, line ->
                    if (forbidden.any { it.containsMatchIn(line) }) "${file.relativeTo(root)}:${i + 1}: ${line.trim()}" else null
                }
            }
            .toList()
        assertTrue("Use Motion.spatial*/effects*/bouncy instead:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
