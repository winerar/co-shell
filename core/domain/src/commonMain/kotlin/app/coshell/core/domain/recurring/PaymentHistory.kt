package app.coshell.core.domain.recurring

import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus

/**
 * «В прошлый раз 41 300 · в среднем 44 000» under the amount of the «Провести» sheet (#404, board `Rec143Post`).
 * [average] — over the last [PAYMENTS_FOR_AVERAGE] payments; `null` while there is only one.
 */
data class PaymentHistory(val last: Money, val average: Money?)

/**
 * Past payments of [templateId] among [transactions]: operations posted by «Провести» or «Проводить само»
 * carry [autoPaymentId], so the id tells which template and day they paid. Only the currency of the latest
 * payment counts (the account may have changed); `null` — nothing was paid yet.
 */
fun paymentHistory(templateId: String, transactions: List<Transaction>): PaymentHistory? {
    val prefix = paymentIdPrefix(templateId)
    // The rest must be the day: template «a» must not pick up payments of a template «a-b».
    val paid = transactions.filter { it.id.startsWith(prefix) && isDay(it.id.removePrefix(prefix)) }
        .sortedByDescending { it.date }
    val last = paid.firstOrNull()?.amount ?: return null
    val recent = paid.map { it.amount }.filter { it.currency == last.currency }.take(PAYMENTS_FOR_AVERAGE)
    val average = if (recent.size < 2) null else Money(recent.sumOf { it.amountMinor }, last.currency).scale(1, recent.size.toLong())
    return PaymentHistory(last, average)
}

/** Reads a year of operations like the template's and finds its payments before [day]. */
class PaymentHistoryUseCase(private val transactions: TransactionRepository) {
    suspend operator fun invoke(template: TransactionTemplate, day: LocalDate, timeZone: TimeZone): PaymentHistory? {
        val filter = TransactionFilter(
            categoryIds = template.categoryId?.let(::setOf),
            from = day.minus(DatePeriod(years = 1)).atStartOfDayIn(timeZone),
            to = day.atStartOfDayIn(timeZone),
            type = template.type,
        )
        return paymentHistory(template.id, transactions.observeTransactions(filter).first())
    }
}

/** Enough to smooth one odd bill, short enough to follow a new tariff. */
const val PAYMENTS_FOR_AVERAGE = 6

private fun isDay(text: String): Boolean = runCatching { LocalDate.parse(text) }.isSuccess
