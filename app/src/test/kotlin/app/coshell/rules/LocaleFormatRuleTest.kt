package app.coshell.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * #469: `"%,d".format(n)` takes the thousands separator from the process locale, so a screenshot's text depended on
 * which test ran before it. Number grouping in app code is always with an explicit locale (`Locale.ROOT` for samples).
 */
class LocaleFormatRuleTest {
    private val sources = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
        .resolve("app/src/main/kotlin")

    @Test
    fun groupingHasAnExplicitLocale() {
        val bad = sources.walkTopDown().filter { it.extension == "kt" }
            .flatMap { f -> f.readLines().mapIndexedNotNull { i, line -> if (implicit(line)) "${f.relativeTo(sources)}:${i + 1}" else null } }
            .toList()
        assertEquals("Add a Locale to grouping format:\n${bad.joinToString("\n")}", emptyList<String>(), bad)
    }

    @Test
    fun theRuleSeesImplicitLocale() {
        assertEquals(true, implicit("""Text("%,d".format(n))"""))
        assertEquals(false, implicit("""Text("%,d".format(java.util.Locale.ROOT, n))"""))
    }

    private fun implicit(line: String) = GROUPING.containsMatchIn(line) && "Locale" !in line

    private companion object {
        val GROUPING = Regex("""%,[\d.]*[df]"\.format\(|String\.format\("%,""")
    }
}
