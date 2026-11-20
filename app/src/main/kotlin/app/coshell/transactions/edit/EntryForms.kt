package app.coshell.transactions.edit

import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.tagIds
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption
import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import app.coshell.transactions.edit.TransactionEditUiState.ToAccountError
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Pure pieces of the entry sheet (#136): the form of a new or loaded operation, error mapping and keypad typing.
// TransactionEditViewModel keeps only loading, state and saving.

/** A new transaction starts on today and on the account used last; the account is chosen first (owner 2026-11-09). */
internal fun newEntryForm(
    base: TransactionEditUiState,
    type: TransactionType,
    accounts: List<Account>,
    recent: List<Transaction>,
    /** «Настройки → Ввод операции → Всегда этот счёт»; `null` — the account used last. */
    preferredAccountId: String? = null,
): TransactionEditUiState {
    val known = accounts.mapTo(HashSet()) { it.id }
    val lastAccount = recent.firstOrNull()?.accountId
    return base.copy(
        isNew = true,
        type = type,
        accountId = preferredAccountId?.takeIf { it in known } ?: lastAccount?.takeIf { it in known } ?: accounts.firstOrNull()?.id,
        date = base.today,
        step = EntryStep.DETAILS,
    )
}

/** The form of a saved [transaction]. An income in an expense category is a refund (#208): an expense with «↩ Возврат». */
internal fun entryFormOf(
    transaction: Transaction,
    base: TransactionEditUiState,
    categories: List<Category>,
    moneyFormat: MoneyFormat,
    timeZone: TimeZone,
): TransactionEditUiState {
    val refund = transaction.type == TransactionType.INCOME &&
        categories.any { it.id == transaction.categoryId && it.type == CategoryType.EXPENSE }
    return base.copy(
        isNew = false,
        step = EntryStep.DETAILS,
        type = if (refund) TransactionType.EXPENSE else transaction.type,
        refund = refund,
        amount = moneyFormat.formatForEditing(transaction.amount),
        toAmount = transaction.toAmount?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
        accountId = transaction.accountId,
        toAccountId = transaction.toAccountId,
        categoryId = transaction.categoryId,
        date = transaction.date.toLocalDateTime(timeZone).date,
        note = transaction.note,
        receiptEnabled = transaction.originalAmount != null,
        receiptCurrency = transaction.originalAmount?.currency?.value.orEmpty(),
        receiptAmount = transaction.originalAmount?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
        tagIds = transaction.tagIds,
    )
}

/** What a keypad [key] (not «Готово») types: the field of [TransactionEditUiState.keypadTarget] and its new text. */
internal fun TransactionEditUiState.typed(key: KeypadKey): Pair<KeypadTarget, String> = when (keypadTarget) {
    KeypadTarget.AMOUNT -> keypadTarget to AmountKeys.apply(amount, key, account?.fractionDigits ?: 2)
    KeypadTarget.TO_AMOUNT -> keypadTarget to AmountKeys.apply(toAmount, key, toAccount?.fractionDigits ?: 2)
    KeypadTarget.RECEIPT -> keypadTarget to AmountKeys.apply(receiptAmount, key, fractionDigitsOf(receiptCurrency))
}

internal fun TransactionEditUiState.withErrors(errors: FormErrors) = copy(
    accountError = errors.accountError,
    amountError = errors.amountError,
    toAccountError = errors.toAccountError,
    toAmountError = errors.toAmountError,
    categoryError = errors.categoryError,
    receiptCurrencyError = errors.receiptCurrencyError,
    receiptAmountError = errors.receiptAmountError,
)

/** Use case refusals that the form could not foresee (e.g. an account deleted on the other phone). */
internal fun TransactionEditUiState.withError(reason: SaveTransactionUseCase.Reason) = when (reason) {
    SaveTransactionUseCase.Reason.ACCOUNT_NOT_FOUND,
    SaveTransactionUseCase.Reason.CURRENCY_MISMATCH,
    -> copy(accountError = true)
    SaveTransactionUseCase.Reason.TARGET_ACCOUNT_NOT_FOUND,
    SaveTransactionUseCase.Reason.TARGET_CURRENCY_MISMATCH,
    -> copy(toAccountError = ToAccountError.REQUIRED)
    SaveTransactionUseCase.Reason.CATEGORY_NOT_FOUND,
    SaveTransactionUseCase.Reason.CATEGORY_TYPE_MISMATCH,
    -> copy(categoryError = true, categoryId = null)
    SaveTransactionUseCase.Reason.ORIGINAL_CURRENCY_INVALID ->
        copy(receiptCurrencyError = TransactionEditUiState.ReceiptCurrencyError.SAME_AS_ACCOUNT)
}

internal fun Account.toOption(balance: String, isNegative: Boolean = false) =
    AccountOption(id, name, currency.value, type, color, balance, Currencies.info(currency).fractionDigits, isNegative)

private fun fractionDigitsOf(code: String): Int =
    CurrencyCode.parseOrNull(code)?.let { Currencies.info(it).fractionDigits } ?: 2
