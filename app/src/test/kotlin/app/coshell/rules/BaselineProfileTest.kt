package app.coshell.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 1.0 check (#383): the startup profile `app/src/main/baseline-prof.txt` is a wildcard over everything in `app.coshell`
 * (#263). It stays current only while all shipped code lives under that package — a class elsewhere would run
 * interpreted on the first launches after every update. A measured profile replaces the wildcard later (#149).
 */
class BaselineProfileTest {
    private val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
    private val profile = root.resolve("app/src/main/baseline-prof.txt")

    @Test
    fun profileCoversTheWholeNamespace() {
        val namespace = Regex("""namespace = "([\w.]+)"""").find(root.resolve("app/build.gradle.kts").readText())!!.groupValues[1]
        val path = namespace.replace('.', '/')
        val rules = profile.readLines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
        assertEquals(listOf("HSPL$path/**;->**(**)**", "L$path/**;"), rules)
    }

    @Test
    fun allShippedCodeIsUnderTheProfiledPackage() {
        val mainSources = root.walkTopDown()
            .onEnter { it.name != "build" && !it.name.startsWith(".") && it.name != "docs" && it.name != "scripts" }
            .filter { it.isFile && it.extension == "kt" && "/src/" in it.invariantSeparatorsPath }
            .filter { file -> file.invariantSeparatorsPath.substringAfter("/src/").substringBefore('/').let { it == "main" || it.endsWith("Main") } }
            .toList()
        assertTrue("no main sources found", mainSources.size > 100)
        val outside = mainSources.mapNotNull { file ->
            val pkg = file.useLines { lines -> lines.firstOrNull { it.startsWith("package ") } }?.removePrefix("package ")?.trim()
            file.relativeTo(root).invariantSeparatorsPath.takeIf { pkg == null || !(pkg == "app.coshell" || pkg.startsWith("app.coshell.")) }
        }
        assertTrue("Outside app.coshell, so not in baseline-prof.txt:\n" + outside.joinToString("\n"), outside.isEmpty())
    }
}
