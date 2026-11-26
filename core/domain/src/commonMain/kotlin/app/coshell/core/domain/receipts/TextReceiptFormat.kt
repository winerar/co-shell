package app.coshell.core.domain.receipts

import app.coshell.core.domain.quickentry.AmountInText
import app.coshell.core.model.CurrencyCode

/**
 * Any printed receipt no exact format knows — the last try before «Не удалось прочитать чек». Rules only, no AI, and still no
 * guessing: all of these must be in the text, or it is not read.
 * - a total line: «ИТОГ», «Итого», «К оплате», «TOTAL», «Total a pagar», «Importe total», «Ընդամենը» (not «Подытог», «Subtotal»)
 *   with a sum on it (the largest number there) or on the next line; several — the largest («TOTAL ITEMS 3» loses to the real total);
 * - the currency: a code at the total («$24.98 USD»), else a sign («$» — pesos, [AmountInText]), else the country's tax id
 *   word — «ИНН» RUB, «CUIT» ARS, «ՀՎՀՀ» AMD, else the language of the receipt — Armenian AMD, Cyrillic RUB, Spanish ARS
 *   (the markets of the app; the sheet shows the currency, and a purchase in another one goes to «Сумма покупки»);
 * - a date ([ReceiptDates]): the one on a «paid» line («Pagado el 16/11/2026» of a bill), else the first printed;
 * - at least [MIN_LINES] lines — one line is a bank message, not a receipt ([AmountInText] reads those).
 * The shop is the first line with letters that is not a title («Чек № 8841», «Comprobante», «Ticket»). Key `text:<hash of the text>`: the same file shared twice is one receipt.
 */
object TextReceiptFormat : ReceiptFormat {
    override val id = "text"

    private const val MIN_LINES = 4
    private const val MERCHANT_MAX = 60

    private val TOTAL = Regex(
        """(?<![\p{L}])(итог(?:о)?|всего к оплате|к оплате|сумма к оплате|total a pagar|importe total|monto total|total|importe(?=\s*:)|ընդամենը)(?![\p{L}])""",
        RegexOption.IGNORE_CASE,
    )
    private val NOT_TOTAL = Regex("""(?<![\p{L}])(под(?:ытог|итог)|промежуточн\p{L}*|sub\s?-?total|total\s+(?:items|art\p{L}*|de\s+art\p{L}*|unidades|позиций))""", RegexOption.IGNORE_CASE)
    private val TAX_ID = listOf(
        Regex("""(?<![\p{L}])ИНН(?![\p{L}])""") to CurrencyCode("RUB"),
        Regex("""(?<![\p{L}])C\.?U\.?I\.?T\.?(?![\p{L}])""", RegexOption.IGNORE_CASE) to CurrencyCode("ARS"),
        Regex("""ՀՎՀՀ""") to CurrencyCode("AMD"),
    )

    private val CODE = Regex("""(?<![\p{L}])(USD|EUR|ARS|AMD|RUB|USDT)(?![\p{L}])""")
    private val PAID = Regex("""(?<![\p{L}])(pagado|pagada|abonado|оплачен\p{L}*|paid)(?![\p{L}])""", RegexOption.IGNORE_CASE)
    private val TITLE = Regex("""^(кассовый\s+)?(чек|квитанция|ticket|factura|comprobante|recibo|receipt|invoice)(?![\p{L}])""", RegexOption.IGNORE_CASE)
    private val SPANISH = Regex("""(?<![\p{L}])(fecha|vuelto|efectivo|sucursal|propina|subtotal|importe|cant|gracias)(?![\p{L}])|[ñáéíóú]""", RegexOption.IGNORE_CASE)

    override fun read(text: String): Receipt? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size < MIN_LINES) return null
        val totals = lines.indices.mapNotNull { i -> totalAt(lines, i) }
        if (totals.isEmpty()) return null
        val currency = totals.firstNotNullOfOrNull { it.currency } ?: taxCurrency(text) ?: languageCurrency(text) ?: return null
        val total = totals.mapNotNull { it.printed.money(currency) }.maxByOrNull { it.amountMinor } ?: return null
        val date = lines.filter { PAID.containsMatchIn(it) }.firstNotNullOfOrNull { ReceiptDates.find(it).firstOrNull() }
            ?: ReceiptDates.find(text).firstOrNull() ?: return null
        val merchant = lines.firstOrNull { line -> line.count(Char::isLetter) >= 3 && !TOTAL.containsMatchIn(line) && !TITLE.containsMatchIn(line) }
            ?.take(MERCHANT_MAX)?.trim() ?: return null
        val normalized = lines.joinToString("\n") { it.replace(Regex("""\s+"""), " ") }
        return Receipt(total = total, date = date, merchant = merchant, key = "$id:${Hash.hex(normalized)}", format = id)
    }

    /** The larger printed number first: «TOTAL 1.234,00 19/11/26» is 1 234, not 26. */
    private val BY_SIZE = compareBy<ReceiptNumbers.Printed>({ it.whole.trimStart('0').length }, { it.whole.trimStart('0') }, { it.fraction.padEnd(8, '0') })

    private class Total(val printed: ReceiptNumbers.Printed, val currency: CurrencyCode?)

    /** The sum of a total line: the last number after the word, or the first one on the next line when the word stands alone. */
    private fun totalAt(lines: List<String>, i: Int): Total? {
        val line = lines[i]
        if (NOT_TOTAL.containsMatchIn(line)) return null
        val word = TOTAL.find(line) ?: return null
        val rest = line.substring(word.range.last + 1)
        val own = ReceiptNumbers.all(rest).maxWithOrNull(BY_SIZE)
        val (printed, source) = when {
            own != null -> own to rest
            i + 1 < lines.size && !TOTAL.containsMatchIn(lines[i + 1]) ->
                (ReceiptNumbers.all(lines[i + 1]).firstOrNull() ?: return null) to lines[i + 1]
            else -> return null
        }
        return Total(printed, currencyOf(source))
    }

    /** A currency written at the sum («1 234,00 ₽», «$ 12.500,00», «AMD 3,500»); `null` when the sum stands bare. */
    private fun currencyOf(text: String): CurrencyCode? {
        CODE.find(text)?.let { return CurrencyCode(it.value) }
        val probe = CurrencyCode("XXX")
        val found = AmountInText.find(text, fallback = probe) ?: return null
        return found.money.currency.takeIf { found.currencyFound && it != probe }
    }

    private fun taxCurrency(text: String): CurrencyCode? = TAX_ID.firstOrNull { it.first.containsMatchIn(text) }?.second

    private fun languageCurrency(text: String): CurrencyCode? = when {
        text.any { it in '\u0531'..'\u058F' } -> CurrencyCode("AMD")
        text.count { it in '\u0400'..'\u04FF' } >= MIN_SCRIPT_LETTERS -> CurrencyCode("RUB")
        SPANISH.findAll(text).take(2).count() >= 2 -> CurrencyCode("ARS")
        else -> null
    }

    private const val MIN_SCRIPT_LETTERS = 10
}
