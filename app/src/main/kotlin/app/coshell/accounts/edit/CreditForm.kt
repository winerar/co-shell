package app.coshell.accounts.edit

import app.coshell.core.model.CreditTerms
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MinPayment
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.MoneyParser

/** The conditions typed in «Условия карты» (#686) → [CreditTerms]; `null` when anything is missing or wrong. Pure: tested without the view model. */
internal fun parseCreditTerms(form: AccountEditUiState, currency: CurrencyCode): CreditTerms? {
    val limit = (MoneyParser.parse(form.creditLimit, currency) as? MoneyParser.Result.Success)?.money?.takeIf { it.isPositive } ?: return null
    val day = form.creditPayDay.trim().toIntOrNull()?.takeIf { it in 1..31 } ?: return null
    val min = if (form.creditMinSum) {
        val sum = (MoneyParser.parse(form.creditMin, currency) as? MoneyParser.Result.Success)?.money?.takeIf { it.isPositive } ?: return null
        MinPayment.Fixed(sum.amountMinor)
    } else {
        MinPayment.Percent(parsePercentBasisPoints(form.creditMin) ?: return null)
    }
    // The grace period is optional: both fields empty is fine, one of them alone or a wrong number refuses the save.
    val statementText = form.creditStatementDay.trim()
    val graceText = form.creditGraceDays.trim()
    if (statementText.isEmpty() && graceText.isEmpty()) return CreditTerms(limit.amountMinor, day, min)
    val statement = statementText.toIntOrNull()?.takeIf { it in 1..31 } ?: return null
    val grace = graceText.toIntOrNull()?.takeIf { it in 1..365 } ?: return null
    return CreditTerms(limit.amountMinor, day, min, statement, grace)
}

/** «5», «5,5», «5.25» → 500, 550, 525 basis points; `null` for anything else, for zero and above 100. */
internal fun parsePercentBasisPoints(text: String): Int? {
    val m = Regex("""^(\d{1,3})(?:[.,](\d{1,2}))?$""").matchEntire(text.trim()) ?: return null
    val bp = m.groupValues[1].toInt() * 100 + m.groupValues[2].padEnd(2, '0').ifEmpty { "0" }.take(2).toInt()
    return bp.takeIf { it in 1..10_000 }
}

/** 500 → «5», 550 → «5,5», 525 → «5,25»: how a saved percent goes back into the field. */
internal fun percentText(basisPoints: Int): String {
    val whole = basisPoints / 100
    val frac = (basisPoints % 100).toString().padStart(2, '0').trimEnd('0')
    return if (frac.isEmpty()) "$whole" else "$whole,$frac"
}

/** The form fields of a saved card's [terms]. */
internal fun AccountEditUiState.withCreditTerms(terms: CreditTerms, currency: CurrencyCode, moneyFormat: MoneyFormat): AccountEditUiState {
    val sum = terms.minPayment as? MinPayment.Fixed
    return copy(
        credit = true,
        creditLimit = moneyFormat.formatForEditing(Money(terms.limitMinor, currency)),
        creditPayDay = terms.payDay.toString(),
        creditStatementDay = terms.statementDay?.toString().orEmpty(),
        creditGraceDays = terms.graceDays?.toString().orEmpty(),
        creditMinSum = sum != null,
        creditMin = sum?.let { moneyFormat.formatForEditing(Money(it.minor, currency)) }
            ?: percentText((terms.minPayment as MinPayment.Percent).basisPoints),
    )
}
