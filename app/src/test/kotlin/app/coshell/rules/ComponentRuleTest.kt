package app.coshell.rules

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Owner's rule (2026-11-15, #367): screens are built only from approved `core:designsystem` components.
 * In `app` it is forbidden to use Material widgets directly, to draw by hand (backgrounds, borders, shapes, canvas,
 * own colours) and to make own text styles. Missing component → mock-up → «ок» → board → design system.
 *
 * Code written before the rule is the baseline `component-rule-baseline.txt` (#367): a file may not get new
 * violations, and a fixed one must leave the baseline (the failure message prints the new baseline).
 */
class ComponentRuleTest {
    private val module = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
        .resolve("app")
    private val sources = module.resolve("src/main/kotlin")
    private val baselineFile = module.resolve("src/test/resources/component-rule-baseline.txt")

    private val current: Map<String, Int> = sources.walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
        val path = file.relativeTo(sources).invariantSeparatorsPath
        violations(file.readText()).map { "$path\t$it" }
    }.groupingBy { it }.eachCount()

    private val baseline: Map<String, Int> = baselineFile.readLines().filter { it.isNotBlank() && !it.startsWith("#") }
        .associate { line -> line.substringBeforeLast('\t') to line.substringAfterLast('\t').toInt() }

    @Test
    fun noNewViolations() {
        val new = current.filter { (key, n) -> n > (baseline[key] ?: 0) }.map { (key, n) -> "$key: $n (baseline ${baseline[key] ?: 0})" }
        assertTrue(
            new.sorted().joinToString("\n", "Not from approved components (use core:designsystem; missing → mock-up first, #367):\n"),
            new.isEmpty(),
        )
    }

    @Test
    fun baselineOnlyShrinks() {
        val fixed = baseline.filter { (key, n) -> (current[key] ?: 0) < n }.keys
        assertTrue("Fixed ${fixed.sorted()} — replace the baseline with:\n${render()}", fixed.isEmpty())
    }

    @Test
    fun theRuleSeesForbiddenCode() {
        val sample = "import androidx.compose.material3.TopAppBar\nimport androidx.compose.material3.Text\n" +
            "Box(Modifier.background(Color.Red, CircleShape)) { Text(\"x\", fontWeight = FontWeight.Bold) }"
        assertTrue(violations(sample).toSet() == setOf("material3 TopAppBar", "draw background", "draw CircleShape", "type fontWeight"))
    }

    private fun render() = "# path<TAB>rule<TAB>count — only shrinks (#367)\n" +
        current.toSortedMap().entries.joinToString("\n") { (key, n) -> "$key\t$n" }

    private companion object {
        /** Material pieces that are not widgets: text and icon primitives, theme access, the screen frame, snackbar state. */
        val ALLOWED_MATERIAL = setOf(
            "Text", "Icon", "MaterialTheme", "Scaffold", "SnackbarHostState", "SnackbarResult", "SnackbarDuration",
            "ExperimentalMaterial3Api", "LocalContentColor", "LocalTextStyle",
        )
        val MATERIAL_IMPORT = Regex("""^import androidx\.compose\.material3?\.(\w+)""", RegexOption.MULTILINE)
        val DRAW = mapOf(
            "background" to Regex("""\.background\("""),
            "border" to Regex("""\.border\("""),
            "clip" to Regex("""\.clip\("""),
            "shadow" to Regex("""\.shadow\("""),
            "CircleShape" to Regex("""\bCircleShape\b(?!\.)"""),
            "RoundedCornerShape" to Regex("""\bRoundedCornerShape\("""),
            "Canvas" to Regex("""\bCanvas\("""),
            "drawBehind" to Regex("""\bdrawBehind\b"""),
            "drawWithContent" to Regex("""\bdrawWithContent\b"""),
            "Brush" to Regex("""\bBrush\."""),
            "Color literal" to Regex("""\bColor\(0x"""),
        )
        val TYPE = mapOf(
            "TextStyle" to Regex("""\bTextStyle\("""),
            "fontSize" to Regex("""\bfontSize\s*="""),
            "fontWeight" to Regex("""\bfontWeight\s*="""),
            "letterSpacing" to Regex("""\bletterSpacing\s*="""),
        )

        fun violations(text: String): List<String> {
            val code = text.lines().map { it.trimStart() }
                .filterNot { it.startsWith("import ") || it.startsWith("//") || it.startsWith("*") || it.startsWith("/*") }
                .joinToString("\n") { it.substringBefore(" // ") }
            val material = MATERIAL_IMPORT.findAll(text).map { it.groupValues[1] }.filter { it !in ALLOWED_MATERIAL }.map { "material3 $it" }
            val draw = DRAW.flatMap { (name, re) -> List(re.findAll(code).count()) { "draw $name" } }
            val type = TYPE.flatMap { (name, re) -> List(re.findAll(code).count()) { "type $name" } }
            return (material + draw + type).toList()
        }
    }
}
