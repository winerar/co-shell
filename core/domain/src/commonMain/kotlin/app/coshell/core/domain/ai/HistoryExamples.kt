package app.coshell.core.domain.ai

import app.coshell.core.domain.categoryguess.CategoryGuess
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType

/** One past entry of the family shown to the model as a hint: what it was called ([label]) and the [category] name it got. */
data class HistoryExample(val label: String, val category: String)

/**
 * The family's own past entries as hints for the model (#583 plan «оболочка» п. 3): the model does not know local shops
 * (Disco, Coto, «Лента»), the history does. Picks up to [MAX] distinct «label → category» pairs from expenses, the ones that
 * look like the asked texts first (a shared word or the same start of a word), then the most frequent. Pure counting, no model.
 */
object HistoryExamples {
    /** Pairs per request: ~10 tokens each keeps the prompt well inside the add-on's 2048-token context. */
    const val MAX = 15

    /** Shorter words say nothing about the shop («с», «за», «de»). */
    private const val MIN_WORD = 3

    /** Two words are «alike» when they share this many first letters (typos at the end: «carrefor» ~ «carrefour»). */
    private const val PREFIX = 4

    private const val MAX_LABEL = 40

    /**
     * [history] newest first; [texts] — what the model is about to read. Only expenses with a live (not archived, present in
     * [categories]) expense category count; a label is the payee, else the first line of the note.
     */
    fun pick(history: List<Transaction>, categories: List<Category>, texts: List<String>, max: Int = MAX): List<HistoryExample> {
        val names = categories.filter { it.type == CategoryType.EXPENSE && !it.archived }.associate { it.id to it.name }
        // Normalized label → (shown label, category id → count); the first seen (newest) spelling is shown.
        val pairs = LinkedHashMap<String, Pair<String, MutableMap<String, Int>>>()
        for (t in history.take(CategoryGuess.WINDOW)) {
            if (t.type != TransactionType.EXPENSE) continue
            val category = t.categoryId?.takeIf { it in names } ?: continue
            val shown = label(t) ?: continue
            val key = CategoryGuess.normalize(shown) ?: continue
            val counts = pairs.getOrPut(key) { shown to mutableMapOf() }.second
            counts[category] = (counts[category] ?: 0) + 1
        }
        if (pairs.isEmpty()) return emptyList()
        val asked = texts.flatMap { words(it) }.toSet()
        data class Scored(val example: HistoryExample, val likeness: Int, val uses: Int, val order: Int)
        val scored = pairs.entries.mapIndexed { order, (key, value) ->
            val (shown, counts) = value
            // The most frequent category of this label; a tie goes to the newer one (inserted first).
            val best = counts.entries.maxBy { it.value }
            Scored(HistoryExample(shown, names.getValue(best.key)), likeness(words(key), asked), counts.values.sum(), order)
        }
        return scored.sortedWith(compareByDescending<Scored> { it.likeness }.thenByDescending { it.uses }.thenBy { it.order })
            .take(max).map { it.example }
    }

    /** The prompt line: `Past entries of this family (name → category): Disco → Продукты | …`; empty when there are none. */
    fun promptLine(examples: List<HistoryExample>): String =
        if (examples.isEmpty()) ""
        else "Past entries of this family (name → category), follow them for the same or similar names: " +
            examples.joinToString(" | ") { "${it.label} → ${it.category}" }

    private fun label(t: Transaction): String? {
        val raw = t.payee?.takeIf { it.isNotBlank() } ?: t.note.lineSequence().firstOrNull { it.isNotBlank() } ?: return null
        return raw.trim().replace('|', ' ').replace('→', ' ').take(MAX_LABEL).trim().ifEmpty { null }
    }

    private fun words(text: String): List<String> =
        CategoryGuess.normalize(text)?.split(' ')?.filter { it.length >= MIN_WORD }.orEmpty()

    /** How many of the label's words match an asked word (the same, or the same first [PREFIX] letters). */
    private fun likeness(label: List<String>, asked: Set<String>): Int =
        label.count { w -> w in asked || (w.length >= PREFIX && asked.any { it.length >= PREFIX && it.take(PREFIX) == w.take(PREFIX) }) }
}
