package app.coshell.core.domain.quickentry

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyParser

/**
 * An amount found in free text. [currencyFound] is `false` when the text named no currency and [money] is in the
 * fallback one (the account's). [note] is the rest of the text for the operation's note.
 */
data class TextAmount(val money: Money, val currencyFound: Boolean, val note: String)

/**
 * Finds the purchase amount in text shared from a bank app or SMS (#320) or pasted from the clipboard (#321):
 * «Compra con tarjeta $12.500,00 en COTO», «Покупка 1 234,56 ₽», «AMD 15,000.00», «13.10 USDT».
 *
 * - Numbers: groups of three split by space, dot, comma or apostrophe, then an optional decimal part;
 *   the separators are resolved by [MoneyParser] for the currency («12.500» in ARS is twelve thousand five hundred).
 * - Currency: a code next to the number (`USD`, `ars`, any of [known]) or a sign: `₽`/`руб` — RUB, `֏`/`драм` — AMD,
 *   `€` — EUR, `US$`/`U$S` — USD, a bare `$` — [dollarSign] (pesos in Argentina, where «$» means ARS).
 * - Which number: the first one with a currency; with none — the first that looks like money (has grouping or
 *   decimals), else the first. Card tails (`*1234`), times (`12:30`) and dates (`13/11`) are skipped.
 */
object AmountInText {

    private val NUMBER = Regex("""(?<![\d.,])(\d{1,3}(?:[ \u00A0\u202F.,']\d{3})+(?:[.,]\d{1,8})?|\d+(?:[.,]\d{1,8})?)(?!\d)""")
    private val SIGN_BEFORE = Regex("""(US\$|U\${'$'}S|\$|€|₽|֏|(?<![A-Za-z])[A-Za-z]{3,4})\s?$""")
    private val SIGN_AFTER = Regex("""^\s?(US\$|\$|€|₽|֏|руб\.?|р\.|драм|[A-Za-z]{3,4}(?![A-Za-z]))""", RegexOption.IGNORE_CASE)
    private val BUILT_IN = setOf("USD", "EUR", "ARS", "AMD", "RUB", "USDT", "BRL", "GEL", "KZT", "TRY")
    private const val NOTE_MAX = 120
    private const val TRIM = " \t\n-–—:·,;|"

    fun find(
        text: String,
        fallback: CurrencyCode,
        known: Set<CurrencyCode> = emptySet(),
        dollarSign: CurrencyCode = CurrencyCode("ARS"),
    ): TextAmount? {
        val codes = BUILT_IN + known.map { it.value }
        val candidates = NUMBER.findAll(text).mapNotNull { candidate(text, it.range, codes, dollarSign) }.toList()
        val best = candidates.firstOrNull { it.currency != null }
            ?: candidates.firstOrNull { it.looksLikeMoney }
            ?: candidates.firstOrNull()
            ?: return null
        val money = MoneyParser.parseOrNull(best.digits, best.currency ?: fallback)?.takeIf { it.isPositive } ?: return null
        val note = (text.substring(0, best.span.first) + " " + text.substring(best.span.last + 1))
            .replace(Regex("""\s+"""), " ").trim { it in TRIM }.take(NOTE_MAX).trim()
        return TextAmount(money, best.currency != null, note)
    }

    private class Candidate(val digits: String, val currency: CurrencyCode?, val span: IntRange, val looksLikeMoney: Boolean)

    private fun candidate(text: String, range: IntRange, codes: Set<String>, dollar: CurrencyCode): Candidate? {
        val before = text.getOrNull(range.first - 1)
        val after = text.getOrNull(range.last + 1)
        if (before == '*' || before == '•' || before == '/' || before == ':' || before == '#') return null
        if (after == ':' || after == '/' || after == '%') return null
        val digits = text.substring(range)
        if (digits.all { it == '0' || !it.isDigit() }) return null

        val head = text.substring(maxOf(0, range.first - 6), range.first)
        val tail = text.substring(range.last + 1, minOf(text.length, range.last + 7))
        var span = range
        var currency: CurrencyCode? = null
        SIGN_BEFORE.find(head)?.let { m ->
            sign(m.groupValues[1], codes, dollar)?.let { currency = it; span = (range.first - (head.length - m.range.first))..span.last }
        }
        if (currency == null) {
            SIGN_AFTER.find(tail)?.let { m ->
                sign(m.groupValues[1], codes, dollar)?.let { currency = it; span = span.first..(range.last + m.range.last + 1) }
            }
        }
        val grouped = digits.length > 3 && digits.any { !it.isDigit() }
        return Candidate(digits, currency, span, grouped || Regex("""[.,]\d{1,2}$""").containsMatchIn(digits))
    }

    private fun sign(raw: String, codes: Set<String>, dollar: CurrencyCode): CurrencyCode? = when (raw.lowercase()) {
        "$" -> dollar
        "us$", "u\$s" -> CurrencyCode("USD")
        "€" -> CurrencyCode("EUR")
        "₽", "руб", "руб.", "р." -> CurrencyCode("RUB")
        "֏", "драм" -> CurrencyCode("AMD")
        else -> raw.uppercase().takeIf { it in codes }?.let(::CurrencyCode)
    }
}
