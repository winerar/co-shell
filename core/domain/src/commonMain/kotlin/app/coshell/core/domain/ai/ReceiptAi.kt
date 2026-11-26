package app.coshell.core.domain.ai

import app.coshell.core.domain.receipts.Receipt
import app.coshell.core.domain.receipts.ReceiptDates
import app.coshell.core.domain.receipts.ReceiptNumbers
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Currencies
import app.coshell.core.model.Money
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * «Прочитать с ИИ» (#593): the text of a receipt no format knows ([app.coshell.core.domain.receipts.ReceiptFormats]) → total, currency, day and shop.
 * The model only proposes; code checks each field: the total must be printed in the text (the model never invents a sum), the currency
 * must be one the person keeps, the day not in the future and not older than a year. A total that fails — no receipt at all; a doubtful
 * day or shop — today / empty. The receipt has no number, so [Receipt.key] is empty: «already recorded» is not checked.
 */
class ReceiptAi(private val model: LanguageModel) {
    val available: Boolean get() = model.available

    /** `true` — the text leaves the phone (the cloud model): the window says so. */
    val cloud: Boolean get() = model.cheap

    /** «Прочитать с ИИ» is on the screen: start loading the model so the button does not wait for it. */
    suspend fun warm() = model.warm()

    /** [known] — currencies of the person's accounts; [fallback] — the currency when the receipt names none of them. */
    suspend fun read(text: String, fallback: CurrencyCode, known: Set<CurrencyCode>, today: LocalDate): Receipt? {
        val body = text.trim().take(MAX_TEXT)
        if (!model.available || body.isEmpty()) return null
        val answer = model.complete(SYSTEM, prompt(body, known + fallback, today)) ?: return null
        return parse(answer, body, fallback, known + fallback, today)
    }

    internal fun prompt(text: String, currencies: Set<CurrencyCode>, today: LocalDate): String = buildString {
        appendLine("Today: $today. Currencies: " + currencies.joinToString(" | ") { it.value })
        appendLine("Receipt text:")
        appendLine(text)
        append("Answer {\"total\":\"<the final sum paid, digits with a dot for decimals>\",\"currency\":\"<one code from the list>\",")
        append("\"date\":\"<the purchase or payment date exactly as printed>\",")
        append("\"shop\":\"<who received the money: the shop, restaurant, service or person — not a product, a fuel type or the payment app>\"}; ")
        append("null for what is not in the text.")
    }

    internal fun parse(answer: String, text: String, fallback: CurrencyCode, currencies: Set<CurrencyCode>, today: LocalDate): Receipt? {
        val from = answer.indexOf('{')
        val to = answer.lastIndexOf('}')
        if (from < 0 || to <= from) return null
        val obj = runCatching { Json.parseToJsonElement(answer.substring(from, to + 1)) }.getOrNull() as? JsonObject ?: return null
        fun field(name: String) = (obj[name] as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() && it != "null" }
        // The model only points at the total; code finds that number in the text and reads it, and the currency printed by it (#742).
        // A total the text does not print — no receipt.
        val found = total(field("total"), text)
        val printed = found.firstOrNull() ?: return null
        // The same sum may stand on several lines («Subtotal $24.98», «Total: $24.98 USD»): the currency is looked for on all of them.
        val currency = currency(field("currency")?.let(CurrencyCode::parseOrNull), found.joinToString("\n") { it.line }, text, currencies, fallback)
        val minor = printed.minor(Currencies.info(currency).fractionDigits)?.takeIf { it > 0 } ?: return null
        val total = Money(minor, currency)
        val date = date(field("date"), text, today)
        val shop = field("shop")?.take(MAX_SHOP)?.takeIf { s -> s.any(Char::isLetter) && !paymentApp(s) } ?: header(text).orEmpty()
        return Receipt(total = total, date = date, merchant = shop, key = "", format = FORMAT)
    }

    /**
     * The printed numbers the model's [value] names: the same sum, or the same digits written otherwise («10.750.50» for «10.750,50»).
     * Empty when the text does not print it: then there is no receipt — a sum the model made up is never taken (#593). Reading the
     * «TOTAL» line without the model was tried (#742) and dropped: it took card digits («Pagado con Mastercard ••8812») and item counts.
     */
    private fun total(value: String?, text: String): List<ReceiptNumbers.Printed> {
        val all = ReceiptNumbers.all(text)
        val model = value?.let { ReceiptNumbers.read(it) }
        fun norm(p: ReceiptNumbers.Printed) = p.whole.trimStart('0') + "." + p.fraction.trimEnd('0')
        return model?.let { m -> all.filter { norm(it) == norm(m) }.ifEmpty { all.filter { it.digits == m.digits } } }.orEmpty()
    }

    /**
     * The currency printed on the total's [line] (all lines that print the sum) when there is one; a bare «$» there — the local [fallback] (#742); else the model's
     * currency if the text prints it; else the only currency the text prints; else [fallback].
     */
    private fun currency(model: CurrencyCode?, line: String, text: String, known: Set<CurrencyCode>, fallback: CurrencyCode): CurrencyCode {
        val onLine = known.filter { printedCurrency(it, line) }
        if (onLine.size == 1) return onLine.single()
        if (model != null && model in onLine) return model
        if ('$' in line) return fallback
        if (model != null && model in known && printedCurrency(model, text)) return model
        return known.filter { printedCurrency(it, text) }.singleOrNull() ?: fallback
    }

    /**
     * The model's date (copied as printed, #742) when the text has it; else the only date of the text; else the first printed one not in the
     * future; else today. Only days within the last year count.
     */
    private fun date(value: String?, text: String, today: LocalDate): LocalDate {
        fun fits(d: LocalDate) = d <= today && d >= today.minus(DatePeriod(years = 1))
        val printed = ReceiptDates.find(text).filter(::fits)
        val model = value?.let { runCatching { LocalDate.parse(it) }.getOrNull() ?: ReceiptDates.parse(it) }?.takeIf(::fits)
        return when {
            model != null && (printed.isEmpty() || model in printed) -> model
            printed.isNotEmpty() -> printed.first()
            else -> today
        }
    }

    companion object {
        /** [Receipt.format] of a receipt the model read. */
        const val FORMAT = "ai"
        private const val MAX_TEXT = 6000
        private const val MAX_SHOP = 60
        private const val MAX_HEADER = 40
        private val NUMBER = Regex("""(?<![\d.,])(\d{1,3}(?:[ \u00A0\u202F.,']\d{3})+(?:[.,]\d{1,8})?|\d+(?:[.,]\d{1,8})?)(?!\d)""")
        private const val SYSTEM = "You read a shop receipt or a payment confirmation for a family budget app. The text may be in any language, " +
            "with columns broken by the PDF. Take the final total actually paid (after discounts, with tax), not a subtotal or a single item. " +
            "Answer with one JSON object only, no comments."

        /** Payment apps and cards: a shop name the model must not give (it is how one paid, not whom). */
        private val PAYMENT_APPS = listOf("mercado pago", "rappi pay", "ualá", "uala", "naranja x", "modo", "visa", "mastercard", "amex", "google pay", "apple pay")

        internal fun paymentApp(shop: String): Boolean = shop.trim().lowercase() in PAYMENT_APPS

        /** Words that start a document's title line, not a shop's name («Чек № 8841», «Comprobante de transferencia»). */
        private val TITLE_WORDS = listOf("чек", "кассовый", "ticket", "tique", "factura", "comprobante", "recibo", "receipt", "invoice", "order", "заказ")

        /**
         * The shop from the receipt's own header when the model gave none (#742): the first of the top three lines that reads like a name —
         * letters, short, not a document title; «YPF FULL - EST. 4410» → «YPF FULL».
         */
        internal fun header(text: String): String? = text.lines().map { it.trim() }.filter { it.isNotEmpty() }.take(3).firstNotNullOfOrNull { line ->
            val name = line.substringBefore(" - ").substringBefore(" / ").trim()
            val lower = name.lowercase()
            name.takeIf {
                it.length in 2..MAX_HEADER && it.count(Char::isLetter) >= 3 && it.count(Char::isDigit) <= 2 &&
                    TITLE_WORDS.none { w -> lower.startsWith(w) } && !paymentApp(it)
            }
        }

        /** Signs that name one currency only («$» is everyone's and does not count); «ФН»/«ФД» — the fiscal numbers of a Russian till receipt. */
        private val SIGNS = mapOf(
            "USD" to listOf("US$", "U\$S", "U\$D", "U\$\$"), "RUB" to listOf("₽", "руб", "ФН ", "ФД "), "AMD" to listOf("֏", "драм"),
            "EUR" to listOf("€"), "ARS" to listOf("AR$"), "GBP" to listOf("£"),
        )

        /** [currency]'s code as a word, or one of its own [SIGNS], is in [text]. */
        internal fun printedCurrency(currency: CurrencyCode, text: String): Boolean {
            if (Regex("(?<![A-Za-z])${currency.value}(?![A-Za-z])").containsMatchIn(text)) return true
            return SIGNS[currency.value].orEmpty().any { text.contains(it, ignoreCase = true) }
        }

        /** The total is one of the numbers in the text: «37.914,50», «37 914,5» or «37914» for 37 914,00. */
        internal fun printed(minor: Long, fractionDigits: Int, text: String): Boolean {
            var unit = 1L
            repeat(fractionDigits) { unit *= 10 }
            val whole = (minor / unit).toString()
            val fraction = (minor % unit).toString().padStart(fractionDigits, '0')
            val forms = buildSet {
                add(whole + fraction)
                add(whole + fraction.trimEnd('0'))
                if (minor % unit == 0L) add(whole)
            }
            return NUMBER.findAll(text).any { it.value.filter(Char::isDigit) in forms }
        }
    }
}
