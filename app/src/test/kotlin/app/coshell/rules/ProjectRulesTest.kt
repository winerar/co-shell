package app.coshell.rules

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Golden rules 1 and 3 of `AGENTS.md`, checked in every `check` (#377):
 * - `core:model` and `core:domain` are pure Kotlin — no `android.*`, `java.*`, `javax.*` (KMP readiness);
 * - money is never floating point: no `Double`/`Float` at all in `core:model`, `core:domain`, `core:data`;
 *   in `app` and `core:designsystem` `Float` is allowed only as screen geometry and shares (`spent / limit`),
 *   while `Double`, `roundToLong` and float parsing/formatting (`toDoubleOrNull`, `"%.2f"`) are banned.
 * Comments and string literals are not code and are skipped. Exceptions — [FLOAT_ALLOWED], each with its reason.
 */
class ProjectRulesTest {
    private val root = File("..").canonicalFile

    @Test
    fun `core model and domain are pure Kotlin`() {
        val found = PURE_MODULES.flatMap { scanDir(it, PLATFORM_API) }
        assertEquals("No android.*/java.*/javax.* in pure modules (kmp-readiness.md):\n" + found.joinToString("\n"), emptyList<String>(), found)
    }

    @Test
    fun `money modules have no floating point`() {
        val found = MONEY_MODULES.flatMap { scanDir(it, ANY_FLOAT) }
        assertEquals("Money is Long minor units (money-and-currency.md):\n" + found.joinToString("\n"), emptyList<String>(), found)
    }

    @Test
    fun `ui modules use Float only for geometry and shares`() {
        val found = UI_MODULES.flatMap { scanDir(it, FLOAT_MONEY_RISK) }
        assertEquals("No Double / float parsing or formatting in UI code (money-and-currency.md):\n" + found.joinToString("\n"), emptyList<String>(), found)
    }

    @Test
    fun `no shadows in ui modules`() {
        val found = UI_MODULES.flatMap { scanDir(it, SHADOW) }
        assertEquals("No shadows (owner 2026-11-16, #477): fill and hairline instead:\n" + found.joinToString("\n"), emptyList<String>(), found)
        assertEquals(listOf(1, 2, 3), scanText("Modifier.shadow(4.dp)\nSurface(shadowElevation = 12.dp)\nfab(elevation = FloatingActionButtonDefaults.elevation())\nSurface(shadowElevation = 0.dp)", SHADOW))
    }

    @Test
    fun `allowed files still exist`() {
        FLOAT_ALLOWED.keys.forEach { assertTrue("stale entry in FLOAT_ALLOWED: $it", File(root, it).isFile) }
    }

    @Test
    fun `the scanner catches what it should`() {
        val bad = """
            import java.math.BigDecimal
            val a = android.os.Build.VERSION.SDK_INT
            val r: Double = 1.0
            fun f(x: Float) = x.roundToLong()
            val t = "1.5".toDoubleOrNull()
            val s = "%.2f".format(x)
        """.trimIndent()
        assertEquals(listOf(1, 2), scanText(bad, PLATFORM_API))
        assertEquals(listOf(3, 4, 5, 6), scanText(bad, ANY_FLOAT))
        assertEquals(listOf(3, 4, 5, 6), scanText(bad, FLOAT_MONEY_RISK))
        val good = """
            // java.util.Currency is not used on purpose: a Double would lose cents
            /* android.* */ val x = 1L
            val label = "java.util and Double in a text"
            val fraction: Float = spent.toFloat() / limit
            val formatted = "%,d".format(n)
            val doubled = DoubleArrow
        """.trimIndent()
        assertEquals(emptyList<Int>(), scanText(good, PLATFORM_API))
        assertEquals(emptyList<Int>(), scanText(good, FLOAT_MONEY_RISK))
    }

    private fun scanDir(dir: String, rule: Regex): List<String> =
        File(root, dir).walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filterNot { it.relativeTo(root).invariantSeparatorsPath in FLOAT_ALLOWED.keys && rule !== PLATFORM_API }
            .flatMap { file -> scanText(file.readText(), rule).map { "${file.relativeTo(root)}:$it" } }
            .toList()

    /** Line numbers (1-based) where [rule] matches code — comments and string literals removed. */
    private fun scanText(text: String, rule: Regex): List<Int> {
        val code = codeOnly(text)
        return code.lines().mapIndexedNotNull { i, line ->
            val hit = rule.containsMatchIn(line) || (rule !== PLATFORM_API && FLOAT_FORMAT.containsMatchIn(text.lines()[i]))
            if (hit) i + 1 else null
        }
    }

    /** [text] with comments and string literal contents blanked out, line breaks kept. */
    private fun codeOnly(text: String): String {
        val out = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                text.startsWith("//", i) -> while (i < text.length && text[i] != '\n') i++
                text.startsWith("/*", i) -> {
                    val end = text.indexOf("*/", i + 2).let { if (it < 0) text.length else it + 2 }
                    text.substring(i, end).forEach { if (it == '\n') out.append('\n') }
                    i = end
                }
                c == '"' -> {
                    val triple = text.startsWith("\"\"\"", i)
                    val quote = if (triple) "\"\"\"" else "\""
                    var j = i + quote.length
                    while (j < text.length && !text.startsWith(quote, j)) j += if (!triple && text[j] == '\\') 2 else 1
                    text.substring(i, minOf(j, text.length)).forEach { if (it == '\n') out.append('\n') }
                    out.append("\"\"")
                    i = minOf(j + quote.length, text.length)
                }
                else -> { out.append(c); i++ }
            }
        }
        return out.toString()
    }

    companion object {
        val PURE_MODULES = listOf("core/model/src", "core/domain/src")
        val MONEY_MODULES = listOf("core/model/src/commonMain", "core/domain/src/commonMain", "core/data/src/commonMain", "core/data/src/jvmMain", "core/data/src/androidMain")
        val UI_MODULES = listOf("app/src/main", "core/designsystem/src/main")

        val PLATFORM_API = Regex("""(^|[^\w.])(android|androidx|java|javax)\.[a-z]""")
        val ANY_FLOAT = Regex("""\b(Double|Float)\b|\.to(Double|Float)(OrNull)?\(|roundToLong|\d\.\d+f?\b""")
        /** Drop shadows: `.shadow(`, a non-zero `shadowElevation`, the FAB / bottom-bar default elevations. */
        val SHADOW = Regex("""\.shadow\(|shadowElevation\s*=\s*[1-9]|FloatingActionButtonDefaults\.(bottomAppBarFab)?[eE]levation\(\)""")
        val FLOAT_MONEY_RISK = Regex("""\bDouble\b|\.toDouble(OrNull)?\(|\.toFloatOrNull\(|roundToLong""")

        /** `"%.2f"`-style format in the raw line (string contents are blanked in [codeOnly]). */
        val FLOAT_FORMAT = Regex(""""[^"]*%[\d.]*f[^"]*"\.format|String\.format\([^)]*%[\d.]*f""")

        /** File → why floating point there is not money. */
        val FLOAT_ALLOWED = mapOf(
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/shake/ShakeDetector.kt" to "accelerometer samples (m/s²)",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/shake/FaceDownDetector.kt" to "accelerometer samples (m/s²)",
            "core/designsystem/src/main/kotlin/app/coshell/core/designsystem/components/MiniBars.kt" to "bar heights: share of the largest value",
            "core/designsystem/src/main/kotlin/app/coshell/core/designsystem/charts/ChartScale.kt" to "chart heights: share of the scale",
            "core/designsystem/src/main/kotlin/app/coshell/core/designsystem/charts/Line.kt" to "line chart points: share of the range",
            "app/src/main/kotlin/app/coshell/update/components/UpdateHeroStep.kt" to "update size in megabytes",
            "app/src/main/kotlin/app/coshell/receipt/document/PdfValues.kt" to "PDF page geometry in points; amounts are read from the text",
            "app/src/main/kotlin/app/coshell/receipt/document/PdfLexer.kt" to "PDF page geometry in points; amounts are read from the text",
            "app/src/main/kotlin/app/coshell/receipt/document/PdfContent.kt" to "PDF page geometry in points; amounts are read from the text",
            "app/src/main/kotlin/app/coshell/receipt/document/PdfText.kt" to "PDF page geometry in points; amounts are read from the text",
            "app/src/main/kotlin/app/coshell/receipt/document/PdfFont.kt" to "PDF page geometry in points; amounts are read from the text",
        )
    }
}
