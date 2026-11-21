package app.coshell.core.domain.receipts

import app.coshell.core.domain.quickentry.AmountInText
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * The PDF «Карточный перевод» of Ameriabank (Armenia), shared from the bank app: «Дата утверждения 26 Октябрь 2026»,
 * «Сумма 2 733.85 AMD», «Детали Ք: Spotify P4856C4BE6 STOCKHOLM 75». A card payment to a merchant shows as a transfer
 * to VISA, so the merchant is the first words of «Детали». The receipt has no number: the key is the day, the debited
 * account, the amount and the details. Sample — `docs/samples/receipts/ameriabank.txt`.
 */
object AmeriabankFormat : ReceiptFormat {
    override val id = "ameriabank"

    private val DATE = Regex("""(?:Дата утверждения|Approval date)\s+(\d{1,2})\s+(\p{L}+)\s+(\d{4})""", RegexOption.IGNORE_CASE)
    private val AMOUNT = Regex("""^\s*(?:Сумма|Amount)\s+(.+?)\s*$""", RegexOption.IGNORE_CASE)
    private val DEBIT = Regex("""^\s*(?:Счёт списания|Счет списания|Debit account)\s+(\d{8,24})\s*$""", RegexOption.IGNORE_CASE)
    private val DETAILS = Regex("""^\s*(?:Детали|Details)\s+(.+?)\s*$""", RegexOption.IGNORE_CASE)
    private val PAYEE = Regex("""^\s*(?:Получатель|Beneficiary)\s+(.+?)\s*$""", RegexOption.IGNORE_CASE)
    private val MONTHS = listOf(
        listOf("январь", "января", "january"), listOf("февраль", "февраля", "february"), listOf("март", "марта", "march"),
        listOf("апрель", "апреля", "april"), listOf("май", "мая", "may"), listOf("июнь", "июня", "june"),
        listOf("июль", "июля", "july"), listOf("август", "августа", "august"), listOf("сентябрь", "сентября", "september"),
        listOf("октябрь", "октября", "october"), listOf("ноябрь", "ноября", "november"), listOf("декабрь", "декабря", "december"),
    )

    override fun read(text: String): Receipt? {
        if (!text.contains("ameriabank", ignoreCase = true)) return null
        val lines = text.lines()
        val date = lines.firstNotNullOfOrNull { DATE.find(it) }?.let { m ->
            val month = MONTHS.indexOfFirst { m.groupValues[2].lowercase() in it } + 1
            if (month == 0) null else runCatching { LocalDate(m.groupValues[3].toInt(), month, m.groupValues[1].toInt()) }.getOrNull()
        } ?: return null
        val total = lines.firstNotNullOfOrNull { l -> AMOUNT.find(l)?.let { money(it.groupValues[1]) } } ?: return null
        val debit = lines.firstNotNullOfOrNull { DEBIT.find(it)?.groupValues?.get(1) } ?: return null
        val details = lines.firstNotNullOfOrNull { DETAILS.find(it)?.groupValues?.get(1) }.orEmpty()
        val merchant = merchantOf(details) ?: lines.firstNotNullOfOrNull { PAYEE.find(it)?.groupValues?.get(1) } ?: "Ameriabank"
        val key = "$id:$date:$debit:${total.amountMinor}:${details.substringAfter(':', details).filter { it.isLetterOrDigit() }.take(40).lowercase()}"
        return Receipt(total = total, date = date, merchant = merchant, key = key, format = id)
    }

    /** «Ք: Spotify P4856C4BE6 STOCKHOLM 75» → «Spotify»: words up to the first one with a digit (a terminal code). */
    private fun merchantOf(details: String): String? =
        details.substringAfter(':', details).trim().split(Regex("""\s+""")).takeWhile { w -> w.none { it.isDigit() } }
            .joinToString(" ").trim().ifEmpty { null }

    private fun money(text: String): Money? =
        AmountInText.find(text, fallback = CurrencyCode("AMD"))?.money?.takeIf { it.isPositive }
}
