package app.coshell.core.domain.receipts

import app.coshell.core.domain.quickentry.AmountInText
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * The PDF receipt of the Teleshka delivery shop (Buenos Aires), shared from Telegram: «ЧЕК #4807», «Дата 14.11.2026 09:42»,
 * lines «Пельмени … x1 $25.000», «Итого $82.400». `$` is pesos. The «Сформировано …» date is when the PDF was made,
 * not the purchase — only the labelled date counts. Sample — `docs/samples/receipts/teleshka.txt`.
 */
object TeleshkaFormat : ReceiptFormat {
    override val id = "teleshka"
    private const val MERCHANT = "Teleshka"
    private val ARS = CurrencyCode("ARS")

    private val NUMBER = Regex("""#\s?(\d{1,12})\b""")
    private val DATE_LINE = Regex("""(?:Дата|Fecha|Date)\s+(\d{1,2})[./](\d{1,2})[./](\d{4})""", RegexOption.IGNORE_CASE)
    private val TOTAL_LINE = Regex("""^\s*(?:Итого|Всего|Total)\s+(-?\$\s?[\d.,   ]+?)\s*$""", RegexOption.IGNORE_CASE)
    private val ITEM_LINE = Regex("""^\s*(.+?)\s+[xх×](\d+(?:[.,]\d+)?)\s+(-?\$\s?[\d.,  ]+)\s*$""")

    override fun read(text: String): Receipt? {
        if (!text.contains(MERCHANT, ignoreCase = true)) return null
        val lines = text.lines()
        val number = lines.firstNotNullOfOrNull { NUMBER.find(it)?.groupValues?.get(1) } ?: return null
        val date = lines.firstNotNullOfOrNull { DATE_LINE.find(it) }?.let { m ->
            val (day, month, year) = m.destructured
            runCatching { LocalDate(year.toInt(), month.toInt(), day.toInt()) }.getOrNull()
        } ?: return null
        val total = lines.firstNotNullOfOrNull { line -> TOTAL_LINE.find(line)?.let { money(it.groupValues[1]) } } ?: return null
        val items = lines.mapNotNull { line ->
            ITEM_LINE.find(line)?.let { m ->
                val amount = money(m.groupValues[3]) ?: return@let null
                ReceiptItem(name = m.groupValues[1].trim(), quantity = m.groupValues[2], amount = amount)
            }
        }
        return Receipt(total = total, date = date, merchant = MERCHANT, key = "$id:$number", format = id, items = items)
    }

    private fun money(text: String): Money? =
        AmountInText.find(text, fallback = ARS, dollarSign = ARS)?.money?.takeIf { it.currency == ARS && it.isPositive }
}
