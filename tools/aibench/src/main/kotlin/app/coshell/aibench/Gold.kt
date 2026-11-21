package app.coshell.aibench

import android.content.Context
import org.json.JSONObject

/** The gold set `docs/samples/ai/eval.json` (copied into assets by Gradle) — the same cases as `scripts/agent/ai_eval.py`. */
class Gold(ctx: Context) {
    class Payee(val payee: String, val category: String)
    class Note(val text: String, val amountMinor: Long, val title: String, val day: Int, val owner: Boolean)
    class Receipt(val text: String, val totalMinor: Long, val currency: String, val date: String, val merchant: String)

    val categories: List<String>
    val payees: List<Payee>
    val notes: List<Note>
    val voice: List<Note>
    val receipts: List<Receipt>

    init {
        fun asset(path: String) = ctx.assets.open(path).bufferedReader().use { it.readText() }
        val root = JSONObject(asset("ai/eval.json"))
        categories = root.getJSONArray("categories").let { a -> List(a.length()) { a.getString(it) } }
        payees = root.getJSONArray("payees").let { a -> List(a.length()) { a.getJSONObject(it).let { o -> Payee(o.getString("payee"), o.getString("category")) } } }
        notes = root.getJSONArray("notes").let { a ->
            List(a.length()) { a.getJSONObject(it).let { o -> Note(o.getString("line"), o.getLong("amount_minor"), o.getString("title"), o.getInt("day"), o.optString("style") == "owner") } }
        }
        voice = root.getJSONArray("voice").let { a ->
            List(a.length()) { a.getJSONObject(it).let { o -> Note(o.getString("phrase"), o.getLong("amount_minor"), o.getString("title"), 0, false) } }
        }
        receipts = root.getJSONArray("receipts").let { a ->
            List(a.length()) {
                val o = a.getJSONObject(it)
                val text = if (o.has("text")) o.getString("text") else asset(o.getString("file").removePrefix("../"))
                Receipt(text, o.getLong("total"), o.getString("currency"), o.getString("date"), o.getString("merchant"))
            }
        }
    }
}
