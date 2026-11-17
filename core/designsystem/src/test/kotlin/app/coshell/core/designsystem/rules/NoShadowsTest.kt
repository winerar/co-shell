package app.coshell.core.designsystem.rules

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Owner's rule (2026-11-16, #477): no shadows in the design — depth is fill and a thin edge only. Elevation of any
 * Material widget must be explicitly `0.dp`; `Modifier.shadow` is forbidden. Gradients — only after the owner's «ок».
 */
class NoShadowsTest {
    private val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }

    private val found: List<String> = listOf("app", "core", "feature").map { File(root, it) }.filter { it.exists() }
        .flatMap { dir -> dir.walkTopDown().filter { it.extension == "kt" && "/src/main/" in it.invariantSeparatorsPath } }
        .flatMap { file -> violations(file.readText()).map { "${file.relativeTo(root).invariantSeparatorsPath}: $it" } }
        .toList()

    @Test
    fun noShadowInTheCode() {
        assertEquals("Shadows are forbidden (#477):\n${found.joinToString("\n")}", emptyList<String>(), found)
    }

    @Test
    fun theRuleSeesShadows() {
        val sample = "Surface(shadowElevation = 12.dp)\nBox(Modifier.shadow(4.dp))\nCard(elevation = CardDefaults.cardElevation())\n" +
            "Fab(elevation = FloatingActionButtonDefaults.elevation())\nSurface(tonalElevation = 0.dp)"
        assertEquals(4, violations(sample).size)
    }

    private companion object {
        val SHADOW = Regex("""\.shadow\(""")
        val ELEVATION = Regex("""\b(shadowElevation|tonalElevation|elevation)\s*=\s*(?!0\.dp|[\w.]*[eE]levation\(0\.dp, 0\.dp, 0\.dp, 0\.dp\))(\S[^\n]*)""")

        fun violations(text: String): List<String> {
            val code = text.lines().map { it.trimStart() }
                .filterNot { it.startsWith("import ") || it.startsWith("//") || it.startsWith("*") || it.startsWith("/*") }
                .joinToString("\n")
            return (SHADOW.findAll(code) + ELEVATION.findAll(code)).map { it.value }.distinct().toList()
        }
    }
}
