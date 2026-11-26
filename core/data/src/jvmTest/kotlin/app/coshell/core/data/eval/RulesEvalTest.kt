package app.coshell.core.data.eval

import app.coshell.core.domain.receipts.ReceiptFormats
import app.coshell.eval.EvalGold
import app.coshell.eval.RulesEval
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The gold set through the app's own rules (#742, `tools/eval`): the set is consistent and the rules and the dictionary keep their
 * score. Raise the thresholds when the rules get better; a drop means a change made quick notes or the category guess worse.
 */
class RulesEvalTest {
    private val samples = File("../../docs/samples/eval").canonicalFile
    private val gold = EvalGold(File(samples, "eval.json").readText()) { File(samples, it).readText() }

    @Test
    fun `the gold set is consistent and its receipts are unknown to the formats`() {
        val names = gold.categories.toSet()
        assertEquals(RulesEval(gold).categories.map { it.name }, gold.categories, "the gold set uses the starter categories of the app")
        assertTrue(gold.notes.all { n -> n.noise || n.categories.isNotEmpty() && n.categories.all { it in names } }, "every note has categories from the list")
        assertTrue(gold.payees.all { p -> p.categories.all { it in names } })
        val known = gold.receipts.filter { ReceiptFormats.read(it.text) != null }.map { it.id }
        assertEquals(emptyList(), known, "the gold receipts are the ones no format reads yet")
    }

    @Test
    fun `the rules keep their score`() {
        val scores = RulesEval(gold).all()
        scores.forEach { s -> println(s.short); s.misses.forEach { println("  ✗ $it") } }
        val ok = scores.associate { it.name to it.ok }
        assertTrue(ok.getValue("заметки (новый человек)") >= NOTES, "notes, new person: $ok")
        assertTrue(ok.getValue("заметки (с историей)") >= NOTES, "notes with history: $ok")
        assertTrue(ok.getValue("категория (новый человек)") >= PAYEES, "payees, new person: $ok")
        assertTrue(ok.getValue("категория (с историей)") >= PAYEES, "payees with history: $ok")
    }

    private companion object {
        const val NOTES = 59
        const val PAYEES = 66
    }
}
