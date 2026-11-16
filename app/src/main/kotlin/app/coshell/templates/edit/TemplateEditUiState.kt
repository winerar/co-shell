package app.coshell.templates.edit

import app.coshell.core.domain.recurring.edited
import app.coshell.core.domain.recurring.nextUnhandled
import app.coshell.core.model.CategoryType
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate

data class TemplateEditUiState(
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val nameError: Boolean = false,
    /** Accounts that can be chosen: not archived. */
    val accounts: List<AccountChoice> = emptyList(),
    val accountId: String? = null,
    val accountError: Boolean = false,
    /** Target of a transfer. */
    val toAccountId: String? = null,
    val toAccountError: Boolean = false,
    /** As typed, in the currency of [accountId]; blank — the amount is asked every time. */
    val amount: String = "",
    val amountError: Boolean = false,
    /**
     * «Платёж в другой валюте» (#415, board `Rec415B`): [amount] is in [amountCurrency], not the account's («Аренда · 650 USD»
     * from an ARS account); what is debited is entered when posting. Not for a transfer.
     */
    val foreign: Boolean = false,
    /** The code typed or picked for [foreign]; kept when the box is unticked, so ticking again brings it back. */
    val amountCurrency: String? = null,
    val amountCurrencyError: Boolean = false,
    /** Chips for [foreign]: currencies of the accounts and of the template; [foreignChoices] drops the account's. */
    val currencyChoices: List<String> = emptyList(),
    /** Every usable category; the screen offers [categoryChoices] — those of the current [type]. */
    val categories: List<CategoryChoice> = emptyList(),
    /** Optional: without it the entry sheet asks for the category. Never set for a transfer. */
    val categoryId: String? = null,
    val note: String = "",
    /** «Повтор» (#143) as it will be saved; `null` — an ordinary template. Built by [withRepeat]. */
    val schedule: RecurringSchedule? = null,
    /** The rule the template had when opened: a changed day counts from its last handled payment. */
    val savedSchedule: RecurringSchedule? = null,
    val today: LocalDate? = null,
    /** Saved or deleted: the screen should close. */
    val isDone: Boolean = false,
) {
    data class AccountChoice(val id: String, val name: String, val currency: String)

    /** [label] — «Папка / Категория» for a subcategory. */
    data class CategoryChoice(val id: String, val label: String, val type: CategoryType)

    val isTransfer: Boolean get() = type == TransactionType.TRANSFER
    val account: AccountChoice? get() = accounts.firstOrNull { it.id == accountId }
    /** [foreign] counts for an expense or income only. */
    val isForeign: Boolean get() = foreign && !isTransfer
    /** The currency [amount] is typed in: [amountCurrency] for a payment in another currency, else the account's. */
    val amountCurrencyShown: String? get() = amountCurrency?.takeIf { isForeign && it.isNotBlank() } ?: account?.currency
    val foreignChoices: List<String> get() = currencyChoices.filter { it != account?.currency }
    val toAccount: AccountChoice? get() = accounts.firstOrNull { it.id == toAccountId }
    val category: CategoryChoice? get() = categories.firstOrNull { it.id == categoryId }

    /**
     * «Проводить само» needs an amount and is not for a transfer (the arriving amount is typed then) nor for a payment in
     * another currency (#415, poll 3 «Д»: what was debited is always entered by hand).
     */
    val canPostAuto: Boolean get() = !isTransfer && !isForeign && amount.isNotBlank()

    /** What the template will do on the payment day: «Проводить само» falls back to «Напомнить» once it cannot. */
    val postMode: PostMode get() = schedule?.mode?.takeIf { it == PostMode.REMIND || canPostAuto } ?: PostMode.REMIND

    val nextPayment: LocalDate? get() = schedule?.nextUnhandled()

    /** «Готово» in the «Повтор» sheet; the same as [preview] of that choice. */
    fun withRepeat(recurrence: Recurrence?, mode: PostMode): TemplateEditUiState = copy(schedule = preview(recurrence, mode))

    fun preview(recurrence: Recurrence?, mode: PostMode): RecurringSchedule? =
        today?.let { savedSchedule.edited(recurrence, if (recurrence == null) PostMode.REMIND else mode, it) }

    val categoryChoices: List<CategoryChoice>
        get() = when (type) {
            TransactionType.EXPENSE -> categories.filter { it.type == CategoryType.EXPENSE }
            TransactionType.INCOME -> categories.filter { it.type == CategoryType.INCOME }
            TransactionType.TRANSFER -> emptyList()
        }
}
