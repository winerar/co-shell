package app.coshell.core.domain.receipts

import app.coshell.core.domain.quickentry.AmountInText
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * The PDF receipt of a T-Bank (Tinkoff) transfer: first line «14.11.2026 20:04:28», «Сумма 12 337 ₽», «Получатель Мария К.»,
 * «Идентификатор операции B6318…». Often a payment for a purchase made as a transfer, so it is read as an expense; the
 * recipient is the merchant. The operation id is the key. Sample — `docs/samples/receipts/tbank.txt`.
 */
object TbankFormat : ReceiptFormat {
    override val id = "tbank"

    private val DATE = Regex("""^\s*(\d{1,2})\.(\d{1,2})\.(\d{4})\s+\d{1,2}:\d{2}""")
    private val AMOUNT = Regex("""^\s*Сумма\s+(.+?)\s*$""")
    private val OPERATION = Regex("""^\s*Идентификатор операции\s+([A-Za-z0-9]{8,64})\s*$""")
    private val RECIPIENT = Regex("""^\s*Получатель\s+(.+?)\s*$""")

    override fun read(text: String): Receipt? {
        if (!text.contains("Идентификатор операции") || !text.contains("tbank", ignoreCase = true)) return null
        val lines = text.lines()
        val date = lines.firstNotNullOfOrNull { DATE.find(it) }?.let { m ->
            runCatching { LocalDate(m.groupValues[3].toInt(), m.groupValues[2].toInt(), m.groupValues[1].toInt()) }.getOrNull()
        } ?: return null
        val total = lines.firstNotNullOfOrNull { l -> AMOUNT.find(l)?.let { money(it.groupValues[1]) } } ?: return null
        val operation = lines.firstNotNullOfOrNull { OPERATION.find(it)?.groupValues?.get(1) } ?: return null
        val merchant = lines.firstNotNullOfOrNull { RECIPIENT.find(it)?.groupValues?.get(1) } ?: "T-Bank"
        return Receipt(total = total, date = date, merchant = merchant, key = "$id:$operation", format = id)
    }

    private fun money(text: String): Money? =
        AmountInText.find(text, fallback = CurrencyCode("RUB"))?.money?.takeIf { it.isPositive }
}
