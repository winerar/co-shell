package app.coshell.language

import app.coshell.wipe.WipeDataUiState
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Every language of [AppLanguage.SUPPORTED] has the same strings as the Russian default
 * (`res/values`), with the same placeholders and the plural forms its grammar needs.
 */
class TranslationsTest {
    private val res = File("src/main/res")

    /** key → placeholders (`%1$s`, `%d`, …) of each text; plurals get one entry per quantity. */
    private class Strings(val texts: Map<String, Set<String>>, val plurals: Map<String, Set<String>>)

    private fun load(dir: String): Strings {
        val texts = mutableMapOf<String, Set<String>>()
        val plurals = mutableMapOf<String, Set<String>>()
        File(res, dir).listFiles { f -> f.name.startsWith("strings") && f.extension == "xml" }!!.forEach { file ->
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            val strings = doc.getElementsByTagName("string")
            for (i in 0 until strings.length) {
                val e = strings.item(i) as Element
                if (e.getAttribute("translatable") == "false") continue
                texts[e.getAttribute("name")] = placeholders(e.textContent)
            }
            val ps = doc.getElementsByTagName("plurals")
            for (i in 0 until ps.length) {
                val e = ps.item(i) as Element
                val items = e.getElementsByTagName("item")
                val quantities = (0 until items.length).map { (items.item(it) as Element).getAttribute("quantity") }
                plurals[e.getAttribute("name")] = quantities.toSet()
                val other = (0 until items.length).map { items.item(it) as Element }.first { it.getAttribute("quantity") == "other" }
                texts["plural:" + e.getAttribute("name")] = placeholders(other.textContent)
            }
        }
        return Strings(texts, plurals)
    }

    private fun placeholders(text: String): Set<String> =
        Regex("""%(\d+\$)?[sdf]""").findAll(text).map { it.value }.toSet()

    private val base = load("values")
    private val requiredPlurals = mapOf("en" to setOf("one", "other"), "es" to setOf("one", "many", "other"))

    @Test
    fun `every language has exactly the default keys`() {
        AppLanguage.SUPPORTED.drop(1).forEach { lang ->
            val translated = load("values-$lang")
            assertEquals("keys of values-$lang", base.texts.keys.sorted(), translated.texts.keys.sorted())
        }
    }

    @Test
    fun `placeholders match the default`() {
        AppLanguage.SUPPORTED.drop(1).forEach { lang ->
            val translated = load("values-$lang")
            base.texts.forEach { (key, args) ->
                assertEquals("placeholders of $key in values-$lang", args, translated.texts[key])
            }
        }
    }

    @Test
    fun `plurals have the forms of their language`() {
        AppLanguage.SUPPORTED.drop(1).forEach { lang ->
            load("values-$lang").plurals.forEach { (key, quantities) ->
                assertEquals("quantities of $key in values-$lang", requiredPlurals.getValue(lang), quantities)
            }
        }
    }

    @Test
    fun `strings folders, SUPPORTED and localeFilters list the same languages`() {
        val folders = res.listFiles { f -> f.isDirectory && f.name.startsWith("values-") && File(f, "strings.xml").isFile }!!
            .map { it.name.removePrefix("values-") }
        assertEquals("values-xx folders", AppLanguage.SUPPORTED.drop(1).sorted(), folders.sorted())
        val filters = Regex("""localeFilters \+= listOf\(([^)]*)\)""").find(File("build.gradle.kts").readText())!!
            .groupValues[1].split(",").map { it.trim().trim('"') }
        assertEquals("localeFilters in app/build.gradle.kts", AppLanguage.SUPPORTED.sorted(), filters.sorted())
    }

    @Test
    fun `the wipe word of every language unlocks the wipe`() {
        AppLanguage.SUPPORTED.forEach { lang ->
            val dir = if (lang == AppLanguage.SUPPORTED.first()) "values" else "values-$lang"
            val word = Regex("""name="wipe_confirm_word">([^<]+)<""").find(File(res, "$dir/strings.xml").readText())!!.groupValues[1]
            assertTrue("$word ($lang)", word.isNotBlank() && WipeDataUiState(word = " ${word.lowercase()} ").canWipe(word))
        }
    }
}
