package app.coshell.accounts.edit

import app.coshell.accounts.edit.AccountEditUiState.BalanceError
import app.coshell.accounts.edit.AccountEditUiState.CurrencyError
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyParser

/** The outcome of checking the account form: an [Account] to save, or the field errors to show. */
internal sealed interface AccountFormResult {
    data class Valid(val account: Account) : AccountFormResult
    data class Invalid(val showErrors: (AccountEditUiState) -> AccountEditUiState) : AccountFormResult
}

/** [MoneyParser] failure → the balance field's error. */
internal fun MoneyParser.Result.Error.toBalanceError(): BalanceError =
    if (reason == MoneyParser.Reason.TOO_MANY_FRACTION_DIGITS) BalanceError.TOO_MANY_FRACTION_DIGITS else BalanceError.INVALID

/**
 * Validates the form (pure): name, currency, the opening balance, card digits and credit terms.
 * [existing] is the edited account (its id and unchanged fields stay); [newId] makes the id of a new one.
 */
internal fun buildAccount(form: AccountEditUiState, existing: Account?, newId: () -> String): AccountFormResult {
    val name = form.name.trim()
    val currency = CurrencyCode.parseOrNull(form.currency)
    var balanceError: BalanceError? = null
    val balanceMinor = when {
        currency == null -> null
        form.initialBalance.isBlank() -> 0L
        else -> when (val parsed = MoneyParser.parse(form.initialBalance, currency)) {
            is MoneyParser.Result.Success -> parsed.money.amountMinor
            is MoneyParser.Result.Error -> {
                balanceError = parsed.toBalanceError()
                null
            }
        }
    }
    if (name.isEmpty() || currency == null || balanceMinor == null) {
        return AccountFormResult.Invalid {
            it.copy(
                nameError = name.isEmpty(),
                currencyError = if (currency == null) CurrencyError.INVALID else null,
                balanceError = balanceError,
            )
        }
    }
    val digits = form.cardDigits.trim()
    if (digits.isNotEmpty() && digits.length != Account.CARD_DIGITS) {
        return AccountFormResult.Invalid { it.copy(cardDigitsError = true) }
    }
    val credit = if (form.credit) parseCreditTerms(form, currency) else null
    if (form.credit && credit == null) return AccountFormResult.Invalid { it.copy(creditError = true) }
    val deposit = if (form.type == AccountType.DEPOSIT && !form.credit && !form.prepaid) {
        when (val parsed = parseDepositTerms(form)) {
            is DepositParse.Valid -> parsed.terms
            DepositParse.Invalid -> return AccountFormResult.Invalid { it.copy(depositError = true) }
        }
    } else {
        null
    }
    val base = existing ?: Account(id = newId(), name = name, currency = currency)
    return AccountFormResult.Valid(
        base.copy(
            credit = credit,
            deposit = deposit,
            cardDigits = digits.ifEmpty { null },
            prepaid = form.prepaid,
            name = name,
            currency = currency,
            type = form.type,
            initialBalanceMinor = balanceMinor,
            excludedFromTotal = !form.includedInTotal,
            color = form.color,
            isShared = form.shared,
            localOnly = form.localOnly,
            // The repository makes the current member the owner of a new personal account.
            ownerMemberId = if (form.shared) null else base.ownerMemberId,
        ),
    )
}
