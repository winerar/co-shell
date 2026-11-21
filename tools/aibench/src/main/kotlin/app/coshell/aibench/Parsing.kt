package app.coshell.aibench

import org.json.JSONObject
import java.math.BigDecimal

/** First `{…}` of the answer as JSON (models may add words around it), `null` when there is none. */
fun jsonOf(answer: String): JSONObject? {
    val from = answer.indexOf('{')
    val to = answer.lastIndexOf('}')
    if (from < 0 || to <= from) return null
    return runCatching { JSONObject(answer.substring(from, to + 1)) }.getOrNull()
}

/** '3500' / '6.99' / '3 500,50' / '82.400' → minor units, `null` if unreadable (same rules as `ai_eval.py`: Argentine «82.400» is thousands). */
fun minorOf(text: String): Long? {
    var t = text.replace(" ", "").replace(" ", "")
    t = when {
        ',' in t && '.' in t -> if (t.lastIndexOf(',') > t.lastIndexOf('.')) t.replace(".", "").replace(',', '.') else t.replace(",", "")
        Regex("""\d{1,3}(\.\d{3})+""").matches(t) -> t.replace(".", "")
        else -> t.replace(',', '.')
    }
    return runCatching { BigDecimal(t).multiply(BigDecimal(100)).setScale(0, java.math.RoundingMode.HALF_UP).toLong() }.getOrNull()
}
