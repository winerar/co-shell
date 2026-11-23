package app.coshell.core.domain.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * What the model made of one quick-note line: a clean [title] and one of the person's [category] names; either may be `null`.
 * [expense] `false` — the model says the line is not a purchase or payment at all (#630: «тест», «позвонить маме»); `null` — not said.
 */
data class NoteHint(val title: String?, val category: String?, val expense: Boolean? = null)

/**
 * The model's share of quick notes (#586, plan «оболочка» п. 2, 4–6): only lines the rules ([app.coshell.core.domain.quickentry.QuickNoteParser])
 * could not finish go to the model, **in one request** per [MAX_LINES] lines; it may only pick a category from the given list; the answer is
 * checked by code and anything doubtful is dropped (the field stays empty, the person fills it).
 * Amounts and dates are never taken from the model.
 */
class NoteHints(private val model: LanguageModel) {
    /** Hints for [lines] (same order); empty list entries are `null`. Without a model or on any failure — all `null`. */
    suspend fun read(lines: List<String>, categories: List<String>): List<NoteHint?> {
        if (lines.isEmpty() || !model.available) return lines.map { null }
        // A long note goes in parts: the add-on's context is 2048 tokens and a small model loses count on long lists.
        return lines.chunked(MAX_LINES).flatMap { part ->
            val answer = model.complete(SYSTEM, prompt(part, categories))
            if (answer == null) part.map { null } else parse(answer, part.size, categories)
        }
    }

    internal fun prompt(lines: List<String>, categories: List<String>): String = buildString {
        appendLine("Categories: " + categories.joinToString(" | "))
        appendLine("For each numbered note give a short title (the shop or thing, no amount, no date) and the best category from the list, or null.")
        appendLine("If a note is not about spending money (a reminder, a test, a random phrase), give \"expense\":false and nothing else.")
        appendLine("Answer: [{\"i\":0,\"title\":\"...\",\"category\":\"...\"}, {\"i\":1,\"expense\":false}, ...]")
        lines.forEachIndexed { i, line -> appendLine("$i: $line") }
    }

    internal fun parse(answer: String, count: Int, categories: List<String>): List<NoteHint?> {
        val result = MutableList<NoteHint?>(count) { null }
        val array = arrayOf(answer) ?: return result
        for (item in array) {
            val obj = item as? JsonObject ?: continue
            val index = (obj["i"] as? JsonPrimitive)?.intOrNull ?: continue
            if (index !in 0 until count || result[index] != null) continue
            val title = (obj["title"] as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() && it.length <= MAX_TITLE && !it.any(Char::isDigit) }
            val category = (obj["category"] as? JsonPrimitive)?.contentOrNull?.trim()
                ?.let { name -> categories.firstOrNull { it.equals(name, ignoreCase = true) } }
            val expense = (obj["expense"] as? JsonPrimitive)?.booleanOrNull
            if (expense == false) result[index] = NoteHint(null, null, expense = false)
            else if (title != null || category != null) result[index] = NoteHint(title, category)
        }
        return result
    }

    /** The first `[…]` of the answer (models add words around it), `null` when it is not a JSON array. */
    private fun arrayOf(answer: String): JsonArray? {
        val from = answer.indexOf('[')
        val to = answer.lastIndexOf(']')
        if (from < 0 || to <= from) return null
        val element: JsonElement = runCatching { Json.parseToJsonElement(answer.substring(from, to + 1)) }.getOrNull() ?: return null
        return element as? JsonArray
    }

    private companion object {
        const val SYSTEM = "You are a precise parser for a family budget app. Notes are short, in any language, often with typos. " +
            "Example: notes \"0: carrefor\", \"1: call mom\", categories \"Groceries | Cafe\" → " +
            "[{\"i\":0,\"title\":\"Carrefour\",\"category\":\"Groceries\"},{\"i\":1,\"expense\":false}]. " +
            "Answer with one JSON array only, no comments."
        const val MAX_TITLE = 60
        /** Lines per request (#587 tuning): about 25 answer tokens each keeps a request well inside the context and ~20 s on a phone. */
        const val MAX_LINES = 10
    }
}
