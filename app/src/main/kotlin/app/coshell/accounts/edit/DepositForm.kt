package app.coshell.accounts.edit

import app.coshell.core.model.DepositPayout
import app.coshell.core.model.DepositTerms
import kotlinx.datetime.LocalDate

/**
 * The conditions typed in «Условия вклада» (#634, step 3) → [DepositTerms]. Nothing typed — `Valid(null)`: a deposit may have no conditions;
 * one of the rate and the day alone, or a wrong number — [DepositParse.Invalid]. Pure: tested without the view model.
 */
internal sealed interface DepositParse {
    data class Valid(val terms: DepositTerms?) : DepositParse
    data object Invalid : DepositParse
}

internal fun parseDepositTerms(form: AccountEditUiState): DepositParse {
    val rateText = form.depositRate.trim()
    val endText = form.depositEnd.trim()
    if (rateText.isEmpty() && endText.isEmpty()) return DepositParse.Valid(null)
    val rate = parsePercentBasisPoints(rateText) ?: return DepositParse.Invalid
    val end = runCatching { LocalDate.parse(endText) }.getOrNull() ?: return DepositParse.Invalid
    return DepositParse.Valid(DepositTerms(rate, end, form.depositPayout))
}

/** The form fields of a saved deposit's [terms]. */
internal fun AccountEditUiState.withDepositTerms(terms: DepositTerms): AccountEditUiState = copy(
    depositRate = percentText(terms.rateBp),
    depositEnd = terms.endDate.toString(),
    depositPayout = terms.payout,
)

internal val DepositPayoutOrder = listOf(DepositPayout.AT_END, DepositPayout.MONTHLY, DepositPayout.CAPITALIZED)
