package app.coshell.transactions.edit

import app.coshell.core.domain.categoryguess.CategoryGuess
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.Draft
import app.coshell.core.model.DraftCategorySource
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * A new form from a draft of a quick note (#586): the amount, account, category, day and the line as the note. An amount in another
 * currency than the account's goes to «Сумма покупки» (the debited sum follows from the rate, as with a receipt).
 * Pure: the view model passes what it has loaded.
 */
internal fun TransactionEditUiState.withNoteDraft(
    draft: Draft,
    accountsById: Map<String, Account>,
    allCategories: List<Category>,
    recentCategoryIds: List<String>,
    converter: CurrencyConverter?,
    moneyFormat: MoneyFormat,
    timeZone: TimeZone,
): TransactionEditUiState {
    val source = draft.categorySource
    val chosenAccountId = draft.accountId?.takeIf { it in accountsById } ?: accountId
    val sameCurrency = chosenAccountId == null || accountsById[chosenAccountId]?.currency == draft.currency
    val typed = draft.money?.let { moneyFormat.formatForEditing(it) }.orEmpty()
    return copy(
        type = TransactionType.EXPENSE,
        accountId = chosenAccountId,
        amount = if (sameCurrency) typed else "",
        receiptEnabled = !sameCurrency && typed.isNotEmpty(),
        receiptCurrency = if (!sameCurrency) draft.currency.value else "",
        receiptAmount = if (!sameCurrency) typed else "",
        categoryId = draft.categoryId?.takeIf { cid -> allCategories.any { it.id == cid } },
        categoryGuessed = source == DraftCategorySource.HISTORY || source == DraftCategorySource.DICTIONARY,
        categoryGuessSource = when (source) {
            DraftCategorySource.HISTORY -> CategoryGuess.Source.HISTORY
            DraftCategorySource.DICTIONARY -> CategoryGuess.Source.DICTIONARY
            else -> null
        },
        date = Instant.fromEpochMilliseconds(draft.dateMillis).toLocalDateTime(timeZone).date,
        note = draft.title,
    ).withDebitSuggestion(converter, moneyFormat).withCategoryOptions(allCategories, recentCategoryIds)
}
