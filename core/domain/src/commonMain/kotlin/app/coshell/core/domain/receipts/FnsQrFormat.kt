package app.coshell.core.domain.receipts

import app.coshell.core.model.CurrencyCode
import kotlinx.datetime.LocalDate

/**
 * The QR of a Russian fiscal receipt (ФНС, 54-ФЗ): `t=20261126T1530&s=1234.56&fn=7380440700076549&i=12345&fp=1234567890&n=1`.
 * Fields in any order, `t` with or without seconds. `n` — 1 purchase, 2 refund of a purchase; 3 and 4 (the shop pays the
 * person) are not read. No shop name in the QR: [Receipt.merchant] is empty, the fiscal drive `fn` (one cash desk, one shop)
 * is the seller for the category guess. Key `fns:<fn>:<i>:<fp>` — the triple the tax service itself checks a receipt by.
 */
object FnsQrFormat : ReceiptFormat {
    override val id = "fns-qr"

    private val TIME = Regex("""^(\d{4})(\d{2})(\d{2})T\d{4}(\d{2})?$""")
    private val SUM = Regex("""^(\d{1,12})(?:\.(\d{1,2}))?$""")
    private val DIGITS = Regex("""^\d{1,20}$""")
    private val RUB = CurrencyCode("RUB")

    override fun read(text: String): Receipt? {
        if (!text.contains("fn=") || !text.contains("fp=") || !text.contains("s=")) return null
        val fields = text.trim().split('&', '?', ' ', '\n').mapNotNull { part ->
            val eq = part.indexOf('=')
            if (eq <= 0) null else part.substring(0, eq).lowercase() to part.substring(eq + 1).trim()
        }.toMap()
        val fn = fields["fn"]?.takeIf(DIGITS::matches) ?: return null
        val number = fields["i"]?.takeIf(DIGITS::matches) ?: return null
        val sign = fields["fp"]?.takeIf(DIGITS::matches) ?: return null
        val refund = when (fields["n"] ?: "1") {
            "1" -> false
            "2" -> true
            else -> return null
        }
        val date = fields["t"]?.let(TIME::find)?.let { m ->
            runCatching { LocalDate(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt()) }.getOrNull()
        }?.takeIf { it.year in 2000..2099 } ?: return null
        val sum = fields["s"]?.let(SUM::find) ?: return null
        val total = ReceiptNumbers.Printed(sum.groupValues[1], sum.groupValues[2], "").money(RUB) ?: return null
        return Receipt(total = total, date = date, merchant = "", key = "$KEY_PREFIX$fn:$number:$sign", format = id, refund = refund)
    }

    /** Receipt keys of this format start so; the part after it up to `:` is the fiscal drive — one cash desk. */
    const val KEY_PREFIX = "fns:"
}
