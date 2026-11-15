package app.coshell.transactions.edit

import app.coshell.core.domain.recurring.PaymentHistory
import app.coshell.core.model.Currencies
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate

/**
 * «Провести» from «Скоро» (#143, board `Rec143Post`): the payment of [date] by the template [name] — the
 * «⟳ Свет · 15 нояб.» pill in the header, «Пропустить этот раз» and the hint under the amount
 * «В прошлый раз [last] · в среднем [average]» (#404; bare numbers, the currency stands next to the amount).
 */
data class PaymentUi(
    val name: String,
    val date: LocalDate,
    val last: String? = null,
    val average: String? = null,
)

internal fun paymentUi(name: String, date: LocalDate, history: PaymentHistory?, format: MoneyFormat) = PaymentUi(
    name = name,
    date = date,
    last = history?.last?.let { format.bare(it) },
    average = history?.average?.let { format.bare(it) },
)

private fun MoneyFormat.bare(money: Money): String =
    formatNumber(money.abs().amountMinor, Currencies.info(money.currency).fractionDigits)
