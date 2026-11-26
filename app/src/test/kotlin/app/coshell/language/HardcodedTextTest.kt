package app.coshell.language

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * No text for people in code (owner 2026-11-14): every visible string is a resource translated into all
 * [AppLanguage.SUPPORTED] languages. Scans the shipped Kotlin of every module and fails on
 * - a Cyrillic string literal (the default language is Russian, so a forgotten text is usually Cyrillic);
 * - a literal with letters passed as visible text: `Text("…")`, `title = "…"`, `contentDescription = "…"`, …
 *   (`$name`, `${expr}` and `%1$s`-style formats are not letters).
 * Not scanned: tests, `@Preview` functions, `*Previews.kt`, `*Samples.kt`, `catalog/` (screenshot-only) and
 * [ALLOWED] — data that is not interface text, each with its reason.
 */
class HardcodedTextTest {
    private val root = File("..").canonicalFile

    @Test
    fun `no text for people is written in code`() {
        val found = sources().flatMap { file -> scan(file).map { "${file.relativeTo(root)}:${it.first}: ${it.second.trim()}" } }
        assertEquals(
            "Move these texts to res/values*/strings*.xml (all languages) and use stringResource:\n" + found.joinToString("\n"),
            emptyList<String>(),
            found,
        )
    }

    @Test
    fun `allowed files still exist`() {
        ALLOWED.keys.forEach { assertTrue("stale entry in ALLOWED: $it", File(root, it).isFile) }
    }

    @Test
    fun `the scanner catches what it should`() {
        val bad = """
            fun Screen() {
                Text("Сохранить")
                Icon(painter, contentDescription = "Close")
                title = "Settings"
            }
        """.trimIndent()
        assertEquals(listOf(2, 3, 4), scanText(bad).map { it.first })
        val good = """
            fun Screen() {
                Text(stringResource(R.string.save))
                Text("${'$'}name ✓")
                Text("%,d".format(n))
                val x by animateFloatAsState(1f, label = "progress")
                // Text("Сохранить") in a comment
            }

            @Preview
            @Composable
            private fun ScreenPreview() = CoShellTheme {
                Text("Превью")
            }
        """.trimIndent()
        assertEquals(emptyList<Pair<Int, String>>(), scanText(good))
    }

    private fun sources(): List<File> =
        listOf(File(root, "app/src"), File(root, "core")).flatMap { dir ->
            dir.walkTopDown()
                .onEnter { it.name != "build" && it.name != "catalog" && !TEST_DIR.matches(it.name) }
                .filter { it.isFile && it.extension == "kt" }
                .filterNot { it.name.endsWith("Previews.kt") || it.name.endsWith("Preview.kt") || it.name.endsWith("Samples.kt") }
                .filterNot { it.relativeTo(root).invariantSeparatorsPath in ALLOWED }
                .toList()
        }

    private fun scan(file: File) = scanText(file.readText())

    /** (line number, line) of every hardcoded text in [text], skipping comments and `@Preview` functions. */
    private fun scanText(text: String): List<Pair<Int, String>> {
        val result = mutableListOf<Pair<Int, String>>()
        var inPreview = false
        var previewDepth = 0
        var previewOpened = false
        var inBlockComment = false
        text.lines().forEachIndexed { index, raw ->
            val line = raw.trim()
            if (inBlockComment) {
                if ("*/" in line) inBlockComment = false
                return@forEachIndexed
            }
            if (line.startsWith("/*")) {
                if ("*/" !in line) inBlockComment = true
                return@forEachIndexed
            }
            if (line.startsWith("//") || line.startsWith("*")) return@forEachIndexed
            if (line.startsWith("@Preview")) {
                inPreview = true; previewDepth = 0; previewOpened = false
                return@forEachIndexed
            }
            if (inPreview) {
                if (line.startsWith("@")) return@forEachIndexed
                val code = withoutStrings(line)
                previewDepth += code.count { it in "({[" } - code.count { it in ")}]" }
                if (code.any { it in "({[" }) previewOpened = true
                if (previewDepth <= 0 && (previewOpened || line.contains("fun "))) inPreview = false
                return@forEachIndexed
            }
            val code = line.substringBefore(" //")
            if (literals(code).any { CYRILLIC.containsMatchIn(it) } || VISIBLE_TEXT.findAll(code).any { hasLetters(it.groupValues[1]) }) {
                result += index + 1 to raw
            }
        }
        return result
    }

    private fun literals(line: String) = Regex("\"((?:[^\"\\\\]|\\\\.)*)\"").findAll(line).map { it.groupValues[1] }

    private fun withoutStrings(line: String) = line.replace(Regex("\"((?:[^\"\\\\]|\\\\.)*)\""), "\"\"")

    private fun hasLetters(literal: String): Boolean =
        literal.replace(Regex("""\$\{[^}]*}"""), "")
            .replace(Regex("""\$[A-Za-z_][A-Za-z0-9_.]*"""), "")
            .replace(Regex("""%(\d+\$)?[,.\d]*[sdf]"""), "")
            .any { it.isLetter() }

    private companion object {
        val TEST_DIR = Regex("test|.*Test")
        val CYRILLIC = Regex("[А-Яа-яЁё]")

        /** Arguments that end up on screen. `label =` is left out: animation labels are debug names. */
        val VISIBLE_TEXT = Regex(
            """(?:\bText\(\s*|\b(?:text|title|subtitle|placeholder|contentDescription|supportingText|message|hint)\s*=\s*)"((?:[^"\\]|\\.)*)"""",
        )

        /** Files whose literals are data, not interface text. */
        val ALLOWED = mapOf(
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/DefaultCategories.kt" to "starter category names in all languages",
            "core/model/src/commonMain/kotlin/app/coshell/core/model/SystemCategories.kt" to "«Корректировка баланса» in all languages",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/quickentry/AmountInText.kt" to "currency words the parser understands",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/quickentry/QuickNoteParser.kt" to "day and account words of a quick note the parser understands (ru/es/en)",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/receipts/TeleshkaFormat.kt" to "labels printed on the shop receipt, read by the parser",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/receipts/ReceiptDates.kt" to "month words printed on receipts (ru/es/en), read by the parser",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/ai/ReceiptAi.kt" to "currency signs printed on receipts (₽, руб, драм), read by the parser",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/receipts/AmeriabankFormat.kt" to "labels printed on the bank receipt, read by the parser",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/receipts/AmeriabankTransferFormat.kt" to "labels printed on the bank receipt, read by the parser",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/receipts/TbankFormat.kt" to "labels printed on the bank receipt, read by the parser",
            "app/src/main/kotlin/app/coshell/wipe/WipeDataUiState.kt" to "confirm words accepted in every language",
            "app/src/main/kotlin/app/coshell/crash/notice/CrashNoticeViewModel.kt" to "issue title prefix read by the developer",
            "core/designsystem/src/main/kotlin/app/coshell/core/designsystem/category/BankColors.kt" to "bank brand names and search aliases",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/categoryguess/BrandsRussiaWorld.kt" to "brand names and kinds of places the dictionary matches against payees (data, #584)",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/categoryguess/BrandsCis.kt" to "brand names of Russia and the CIS the dictionary matches (data, #637)",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/categoryguess/WordsEveryday.kt" to "everyday words of a quick note the dictionary matches (data, #637)",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/categoryguess/SoundKey.kt" to "Cyrillic → Latin letters for loose matching (#631), not interface text",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/quickentry/CurrencyWords.kt" to "currency words the parser understands (#631)",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/drafts/AccountMention.kt" to "generic words of account names the parser skips (#632)",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/ai/NoteHints.kt" to "payment slang the model is told about in its prompt (#635), not interface text",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/categoryguess/BrandDictionary.kt" to "letter folding for matching payee names («ё» → «е»), not interface text",
            "core/domain/src/commonMain/kotlin/app/coshell/core/domain/update/NoteEntries.kt" to "markup of release notes written by the team (\"Исправлено:\"), not interface text",
        )
    }
}
