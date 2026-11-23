package app.coshell.core.domain.receipts

import app.coshell.core.domain.quickentry.AmountInText
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * The PDF «Перевод между своими счетами» of Ameriabank (#646): «Дата создания 2026-11-23 13:26:55», «Счёт списания 4000********0001»,
 * «Счёт зачисления 4000********0002», «Сумма списания 3,000.00 AMD», «Сумма зачисления …», «Детали Transfer of own funds».
 * Not a purchase: [Receipt.transfer] is set, the sheet opens as «Перевод». The key is the creation time, both accounts and the sum.
 * Sample — `docs/samples/receipts/ameriabank-own-transfer.txt`.
 */
object AmeriabankTransferFormat : ReceiptFormat {
    override val id = "ameriabank-own"

    private val CREATED = Regex("""^\s*(?:Дата создания|Creation date)\s+(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2}):(\d{2})""", RegexOption.IGNORE_CASE)
    private val DEBIT = Regex("""^\s*(?:Сч[её]т списания|Debit account)\s+([\d*•xX ]{8,32})\s*$""", RegexOption.IGNORE_CASE)
    private val CREDIT = Regex("""^\s*(?:Сч[её]т зачисления|Credit account)\s+([\d*•xX ]{8,32})\s*$""", RegexOption.IGNORE_CASE)
    private val OUT = Regex("""^\s*(?:Сумма списания|Debit amount)\s+(.+?)\s*$""", RegexOption.IGNORE_CASE)
    private val IN = Regex("""^\s*(?:Сумма зачисления|Credit amount)\s+(.+?)\s*$""", RegexOption.IGNORE_CASE)

    override fun read(text: String): Receipt? {
        if (!text.contains("ameriabank", ignoreCase = true) || !text.contains("между своими счетами", ignoreCase = true)) return null
        val lines = text.lines()
        val created = lines.firstNotNullOfOrNull { CREATED.find(it) } ?: return null
        val (y, mo, d, h, mi, s) = created.destructured
        val date = runCatching { LocalDate(y.toInt(), mo.toInt(), d.toInt()) }.getOrNull() ?: return null
        val from = lines.firstNotNullOfOrNull { DEBIT.find(it)?.groupValues?.get(1) }?.let(Account::digitsOf) ?: return null
        val to = lines.firstNotNullOfOrNull { CREDIT.find(it)?.groupValues?.get(1) }?.let(Account::digitsOf) ?: return null
        val out = lines.firstNotNullOfOrNull { l -> OUT.find(l)?.let { money(it.groupValues[1]) } } ?: return null
        val into = lines.firstNotNullOfOrNull { l -> IN.find(l)?.let { money(it.groupValues[1]) } } ?: out
        val key = "$id:$y$mo$d$h$mi$s:$from:$to:${out.amountMinor}"
        return Receipt(
            total = out, date = date, merchant = "Ameriabank", key = key, format = id,
            debitDigits = from, transfer = ReceiptTransfer(toDigits = to, toTotal = into),
        )
    }

    private fun money(text: String): Money? =
        AmountInText.find(text, fallback = CurrencyCode("AMD"))?.money?.takeIf { it.isPositive }
}
