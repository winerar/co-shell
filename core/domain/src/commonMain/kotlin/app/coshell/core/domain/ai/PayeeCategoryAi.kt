package app.coshell.core.domain.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * The category of a payee nothing else knows (#594, «Starbucks» → «Кафе»): one request with the person's category names and the shop or note
 * text; the answer must be one of those names, anything else — `null` and the field stays for the person. Asked only after past entries
 * and the brand dictionary ([app.coshell.core.domain.categoryguess.CategoryGuess]) found nothing.
 */
class PayeeCategoryAi(private val model: LanguageModel) {
    val available: Boolean get() = model.available

    /**
     * The name from [categories] that fits [text], `null` when the model is not sure, failed, or named something else.
     * [examples] — the family's past «name → category» pairs ([HistoryExamples]): local shops the model does not know.
     */
    suspend fun suggest(text: String, categories: List<String>, examples: List<HistoryExample> = emptyList(), categoryHints: String = ""): String? {
        val shop = text.trim().take(MAX_TEXT)
        if (!model.available || shop.isEmpty() || categories.isEmpty()) return null
        val answer = // The family's past entries stay on the phone: the cloud model never gets them (`cloud-ai.md` §«Правила», #742).
        model.complete(SYSTEM, prompt(shop, categories, if (model.cheap) emptyList() else examples, categoryHints)) ?: return null
        return parse(answer, categories)
    }

    internal fun prompt(text: String, categories: List<String>, examples: List<HistoryExample> = emptyList(), categoryHints: String = ""): String = buildString {
        appendLine("Categories: " + categories.joinToString(" | "))
        if (categoryHints.isNotEmpty()) appendLine(categoryHints)
        HistoryExamples.promptLine(examples).takeIf { it.isNotEmpty() }?.let(::appendLine)
        appendLine("Shop or note: $text")
        appendLine("If no category clearly fits, give the most general one of the list (like \"Other\"), not a near guess.")
        append("Answer {\"category\":\"<one name from the list>\"}, or {\"category\":null} if you are not sure.")
    }

    internal fun parse(answer: String, categories: List<String>): String? {
        val from = answer.indexOf('{')
        val to = answer.lastIndexOf('}')
        if (from < 0 || to <= from) return null
        val obj = runCatching { Json.parseToJsonElement(answer.substring(from, to + 1)) }.getOrNull() as? JsonObject ?: return null
        val name = (obj["category"] as? JsonPrimitive)?.contentOrNull?.trim() ?: return null
        return categories.firstOrNull { it.equals(name, ignoreCase = true) }
    }

    private companion object {
        const val SYSTEM = "You pick the spending category of a shop or payee for a family budget app. Names may be in any language, with typos or " +
            "card-statement noise like \"MERPAGO*KIOSCO 123\". Example: shop \"Starbucks\", categories \"Groceries | Cafe | Taxi\" → {\"category\":\"Cafe\"}. " +
            "Answer with one JSON object only, no comments."
        const val MAX_TEXT = 60
    }
}

/** No model at all (tests, previews): nothing is suggested. */
object NoLanguageModel : LanguageModel {
    override val available: Boolean = false
    override suspend fun complete(system: String, prompt: String): String? = null
}
