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
 * [account] — one of the accounts' labels the line names or implies as the way of paying («с карты мамы», «наличкой», «долларами»); `null` — not said.
 */
data class NoteHint(val title: String?, val category: String?, val expense: Boolean? = null, val account: String? = null)

/**
 * The model's share of quick notes (#586, plan «оболочка» п. 2, 4–6): only lines the rules ([app.coshell.core.domain.quickentry.QuickNoteParser])
 * could not finish go to the model, **in one request** per [MAX_LINES] lines; it may only pick a category from the given list; the answer is
 * checked by code and anything doubtful is dropped (the field stays empty, the person fills it).
 * Amounts and dates are never taken from the model.
 */
class NoteHints(private val model: LanguageModel) {
    /** The model is cheap (cloud): the caller sends every line, not only the unfinished ones (#635). */
    val broad: Boolean get() = model.cheap

    /** The cloud refused the last request: the daily limit is over (#635). */
    val limited: Boolean get() = model.limited

    /**
     * Hints for [lines] (same order); empty list entries are `null`. Without a model or on any failure — all `null`.
     * [accounts] — labels of the person's live accounts; the model may name one as the way of paying (never invent one).
     */
    suspend fun read(lines: List<String>, categories: List<String>, accounts: List<String> = emptyList()): List<NoteHint?> {
        if (lines.isEmpty() || !model.available) return lines.map { null }
        // A long note goes in parts: the add-on's context is 2048 tokens and a small model loses count on long lists.
        return lines.chunked(MAX_LINES).flatMap { part ->
            val answer = model.complete(if (accounts.isEmpty()) SYSTEM else SYSTEM_ACCOUNTS, prompt(part, categories, accounts))
            if (answer == null) part.map { null } else parse(answer, part.size, categories, accounts)
        }
    }

    internal fun prompt(lines: List<String>, categories: List<String>, accounts: List<String> = emptyList()): String = buildString {
        appendLine("Categories: " + categories.joinToString(" | "))
        if (accounts.isNotEmpty()) {
            appendLine("Accounts (name · currency · kind): " + accounts.joinToString(" | "))
            appendLine("If a note says or clearly implies how it was paid (a card, cash, a bank, an app, a person's card, a currency), give the best matching account exactly as written in the list as \"account\"; if it says nothing, leave it out.")
        }
        appendLine("For each numbered note give a short title (the shop or thing, no amount, no date) and the best category from the list, or null.")
        appendLine("If a note is not about spending money (a reminder, a test, a random phrase), give \"expense\":false and nothing else.")
        appendLine(if (accounts.isEmpty()) "Answer: [{\"i\":0,\"title\":\"...\",\"category\":\"...\"}, {\"i\":1,\"expense\":false}, ...]"
            else "Answer: [{\"i\":0,\"title\":\"...\",\"category\":\"...\",\"account\":\"...\"}, {\"i\":1,\"expense\":false}, ...]")
        lines.forEachIndexed { i, line -> appendLine("$i: $line") }
    }

    internal fun parse(answer: String, count: Int, categories: List<String>, accounts: List<String> = emptyList()): List<NoteHint?> {
        val result = MutableList<NoteHint?>(count) { null }
        val array = arrayOf(answer) ?: return result
        for (item in array) {
            val obj = item as? JsonObject ?: continue
            val index = (obj["i"] as? JsonPrimitive)?.intOrNull ?: continue
            if (index !in 0 until count || result[index] != null) continue
            val title = (obj["title"] as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() && it.length <= MAX_TITLE && !it.any(Char::isDigit) }
            val category = (obj["category"] as? JsonPrimitive)?.contentOrNull?.trim()
                ?.let { name -> categories.firstOrNull { it.equals(name, ignoreCase = true) } }
            val account = (obj["account"] as? JsonPrimitive)?.contentOrNull?.trim()
                ?.let { label -> accounts.firstOrNull { it.equals(label, ignoreCase = true) } }
            val expense = (obj["expense"] as? JsonPrimitive)?.booleanOrNull
            if (expense == false) result[index] = NoteHint(null, null, expense = false)
            else if (title != null || category != null || account != null) result[index] = NoteHint(title, category, account = account)
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
        const val SYSTEM_ACCOUNTS = "You are a precise parser for a family budget app. Notes are short, in any language, often with typos. " +
            "Example: notes \"0: carrefor mp\", \"1: call mom\", categories \"Groceries | Cafe\", accounts \"Mercado Pago · ARS · card | Cash · ARS · cash\" → " +
            "[{\"i\":0,\"title\":\"Carrefour\",\"category\":\"Groceries\",\"account\":\"Mercado Pago · ARS · card\"},{\"i\":1,\"expense\":false}]. " +
            "Answer with one JSON array only, no comments."
        const val MAX_TITLE = 60
        /** Lines per request (#587 tuning): about 25 answer tokens each keeps a request well inside the context and ~20 s on a phone. */
        const val MAX_LINES = 10
    }
}
