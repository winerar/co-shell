package app.coshell.aieval

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * The gold set `docs/samples/ai/eval.json` (#742): what the app must get from each note, payee and receipt. Shared by the phone bench
 * (`tools/aibench`) and the JVM tests of `core:domain`, so both measure the same thing. [readFile] reads `file` entries relative to
 * `docs/samples/ai/` (a receipt kept as its own text file).
 */
class EvalGold(json: String, readFile: (String) -> String) {
    /**
     * [categories] — every name that counts as right (an ambiguous line: «десерты» — café or groceries); [pay] — `card`, `cash` or `null`;
     * [noise] — not a spending at all («позвонить маме»): the app must leave it out.
     */
    class Note(
        val line: String, val amountMinor: Long, val title: String, val day: Int, val categories: List<String>, val pay: String?,
        val owner: Boolean, val noise: Boolean = false,
    )

    /** [categories] — the right one first, then other names that also count (a plant nursery: home or gifts). */
    class Payee(val payee: String, val categories: List<String>) {
        val category: String get() = categories.first()
    }
    /** [merchants] — the shop's name; a receipt printing it in two scripts («ԵՐԵՎԱՆ ՍԻԹԻ / YEREVAN CITY») lists both. */
    class Receipt(val id: String, val text: String, val totalMinor: Long, val currency: String, val date: String, val merchants: List<String>) {
        val merchant: String get() = merchants.first()
    }

    val categories: List<String>
    val notes: List<Note>
    val payees: List<Payee>
    val receipts: List<Receipt>

    init {
        val root = Json.parseToJsonElement(json).jsonObject
        fun JsonObject.str(key: String) = (this[key] as? JsonPrimitive)?.contentOrNull
        categories = root.getValue("categories").jsonArray.map { it.jsonPrimitive.content }
        payees = root.getValue("payees").jsonArray.map { e ->
            val o = e.jsonObject
            Payee(o.str("payee")!!, listOf(o.str("category")!!) + (o["also"] as? JsonArray).orEmpty().map { it.jsonPrimitive.content })
        }
        notes = root.getValue("notes").jsonArray.map { e ->
            val o = e.jsonObject
            val cats = when (val c = o["category"]) {
                is JsonArray -> c.map { it.jsonPrimitive.content }
                is JsonPrimitive -> listOf(c.content)
                else -> emptyList()
            }
            Note(
                line = o.str("line")!!, amountMinor = (o["amount_minor"] as? JsonPrimitive)?.longOrNull ?: 0, title = o.str("title").orEmpty(),
                day = (o["day"] as? JsonPrimitive)?.intOrNull ?: 0, categories = cats,
                pay = o.str("pay")?.takeIf { it != "none" }, owner = o.str("style") == "owner", noise = o.str("noise") == "true",
            )
        }
        receipts = root.getValue("receipts").jsonArray.map { e ->
            val o = e.jsonObject
            val text = o.str("text") ?: readFile(o.str("file")!!)
            Receipt(
                o.str("id")!!, text, (o["total"] as JsonPrimitive).longOrNull!!, o.str("currency")!!, o.str("date")!!,
                listOf(o.str("merchant")!!) + (o["merchant_also"] as? JsonArray).orEmpty().map { it.jsonPrimitive.content },
            )
        }
    }
}
