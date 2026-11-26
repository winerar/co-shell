package app.coshell.core.data.aieval

import app.coshell.aieval.AiEval
import app.coshell.aieval.EvalGold
import app.coshell.aieval.ReplayModel
import app.coshell.core.domain.receipts.ReceiptFormats
import app.coshell.core.domain.receipts.TextReceiptFormat
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * The AI gold set through the app's own code (#742) without a phone: the set is consistent, the rules alone keep their score, and model
 * answers recorded on a phone (`docs/samples/ai/answers/<model>.json`, «Поделиться ответами» in the bench) replay to scores not worse than on the phone — so a change
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
        val known = gold.receipts.filter { ReceiptFormats.readExact(it.text) != null }.map { it.id }
        assertEquals(emptyList(), known, "receipts read by an exact format are not «unknown»")
        assertTrue(gold.receipts.size >= 19)
    }

    @Test
    fun `the printed receipt rules read the gold receipts and never wrong`() {
        val read = gold.receipts.mapNotNull { g -> TextReceiptFormat.read(g.text)?.let { g to it } }
        val wrong = read.filterNot { (g, r) ->
            r.total.amountMinor == g.totalMinor && r.total.currency.value == g.currency && r.date.toString() == g.date
        }.map { it.first.id }
        assertEquals(emptyList(), wrong, "a receipt read wrong is worse than one not read")
        // Raise when the rules get better: 17 of 19 on 2026-11-26 (a transfer and a bank SMS are no receipts).
        assertTrue(read.size >= RULES_RECEIPTS, "read by rules: ${read.map { it.first.id }}")
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
    fun `recorded phone answers replay to scores not worse than on the phone`() = runTest {
        val files = File(samples, "answers").listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }
        for (file in files) {
            val text = file.readText()
            val expected = EXPECTED.find(text)?.groupValues?.get(1)?.split(", ")?.toSet() ?: continue
            val recorded = expected.associate { it.substringBeforeLast(' ') to it.substringAfterLast(' ').substringBefore('/').toInt() }
            println("== ${file.name}")
            // Each measure on its own: a prompt changed for notes leaves the recorded receipts still checked.
            val measures = listOf<suspend (AiEval) -> AiEval.Score>({ it.notes(false) }, { it.notes(true) }, { it.payees(false) }, { it.payees(true) }, { it.receipts() })
            for (measure in measures) {
                val replay = ReplayModel(text)
                val s = measure(AiEval(gold, replay))
                if (replay.missing > 0) {
                    println("${s.name}: ${replay.missing} requests are not in the recording (prompt changed) — record again on the phone")
                    continue
                }
                println(s.line)
                s.misses.forEach { println("  ✗ $it") }
                // Not worse than on the phone: the code may only read the same answers better (#742).
                assertTrue(s.ok >= (recorded[s.name] ?: 0), "${file.name}: ${s.short}, on the phone ${recorded[s.name]}")
            }
        }
    }

    private companion object {
        val EXPECTED = Regex(""""scores"\s*:\s*"([^"]*)"""")
        const val RULES_NOTES_NEW = 59
        const val RULES_NOTES_HISTORY = 59
        const val RULES_PAYEES_NEW = 66
        const val RULES_PAYEES_HISTORY = 66
        const val RULES_RECEIPTS = 17
    }
}
