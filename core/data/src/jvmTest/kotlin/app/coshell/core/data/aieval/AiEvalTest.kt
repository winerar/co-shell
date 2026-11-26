package app.coshell.core.data.aieval

import app.coshell.aieval.AiEval
import app.coshell.aieval.EvalGold
import app.coshell.aieval.ReplayModel
import app.coshell.core.domain.receipts.ReceiptFormats
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * The AI gold set through the app's own code (#742) without a phone: the set is consistent, the rules alone keep their score, and model
 * answers recorded on a phone (`docs/samples/ai/answers/<model>.json`, «Поделиться ответами» in the bench) replay to the same scores — so a change
 * of a parser or a check that would make the app worse fails here. A recording whose prompts no longer match is reported and skipped.
 */
class AiEvalTest {
    private val samples = File("../../docs/samples/ai").canonicalFile
    private val gold = EvalGold(File(samples, "eval.json").readText()) { File(samples, it).readText() }

    @Test
    fun `the gold set is consistent and its receipts are unknown to the formats`() {
        val names = gold.categories.toSet()
        assertEquals(AiEval(gold, null).categories.map { it.name }, gold.categories, "the gold set uses the starter categories of the app")
        assertTrue(gold.notes.all { n -> n.noise || n.categories.isNotEmpty() && n.categories.all { it in names } }, "every note has categories from the list")
        assertTrue(gold.payees.all { p -> p.categories.all { it in names } })
        val known = gold.receipts.filter { ReceiptFormats.read(it.text) != null }.map { it.id }
        assertEquals(emptyList(), known, "receipts read by a format are not «unknown»: they never reach the model")
        assertTrue(gold.receipts.size >= 19)
    }

    @Test
    fun `the rules alone keep their score`() = runTest {
        val eval = AiEval(gold, model = null)
        val scores = eval.all()
        scores.forEach { s -> println(s.line); s.misses.forEach { println("  ✗ $it") } }
        val ok = scores.associate { it.name to it.ok }
        // Raise these when the rules get better; a drop means a change made the app worse without any model.
        assertTrue(ok.getValue("заметки (новый человек)") >= RULES_NOTES_NEW, "notes, new person: ${ok}")
        assertTrue(ok.getValue("заметки (с историей)") >= RULES_NOTES_HISTORY, "notes with history: ${ok}")
        assertTrue(ok.getValue("категория (новый человек)") >= RULES_PAYEES_NEW, "payees, new person: ${ok}")
        assertTrue(ok.getValue("категория (с историей)") >= RULES_PAYEES_HISTORY, "payees with history: ${ok}")
        assertEquals(0, ok.getValue("чеки"), "without a model no unknown receipt is read")
    }

    @Test
    fun `recorded phone answers replay to their scores`() = runTest {
        val files = File(samples, "answers").listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }
        for (file in files) {
            val text = file.readText()
            val expected = EXPECTED.find(text)?.groupValues?.get(1)?.split(", ")?.toSet() ?: continue
            val replay = ReplayModel(text)
            val eval = AiEval(gold, replay)
            val got = eval.all()
            if (replay.missing > 0) {
                println("${file.name}: ${replay.missing} requests are not in the recording (prompts changed) — record again on the phone")
                continue
            }
            got.forEach { println(it.line) }
            assertEquals(expected, got.map { it.short }.toSet(), file.name)
        }
    }

    private companion object {
        val EXPECTED = Regex(""""scores"\s*:\s*"([^"]*)"""")
        const val RULES_NOTES_NEW = 55
        const val RULES_NOTES_HISTORY = 55
        const val RULES_PAYEES_NEW = 60
        const val RULES_PAYEES_HISTORY = 60
    }
}
