package app.coshell.transactions.edit

import app.coshell.core.model.AccountType
import app.coshell.core.model.TransactionType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus

data class TransactionEditUiState(
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    /** A copy of another operation («Дублировать»): the header says «Копия · сегодня». */
    val isCopy: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    /**
     * «↩ Возврат» on an expense (#208, board Fb208C): money came back for a purchase. Saved as an income in the
     * chosen expense category; the sheet keeps the expense categories and shows the amount in the income colour.
     */
    val refund: Boolean = false,
    /** Amount as typed, in the currency of [accountId]. */
    val amount: String = "",
    /** [amount] was filled from the receipt by the rate ([withDebitSuggestion]), not typed. */
    val amountSuggested: Boolean = false,
    /** Date of the rate behind a suggested [amount], for the "по курсу на …" hint. */
    val suggestedRateDate: LocalDate? = null,
    /** Amount arriving to [toAccountId]; asked only when the two accounts differ in currency. */
    val toAmount: String = "",
    val accounts: List<AccountOption> = emptyList(),
    val accountId: String? = null,
    val toAccountId: String? = null,
    /** Top-level categories of the current type with their subcategories. */
    val categories: List<CategoryGroup> = emptyList(),
    val categoryId: String? = null,
    /** Folder whose subcategories are shown: the selected folder or the folder of the selected subcategory. */
    val openFolderId: String? = null,
    val date: LocalDate = LocalDate(1970, 1, 1),
    val today: LocalDate = LocalDate(1970, 1, 1),
    val note: String = "",
    /** Paid in another currency than the account's: the receipt amount is asked too (not for transfers). */
    val receiptEnabled: Boolean = false,
    val receiptCurrency: String = "",
    /** Amount as on the receipt, in [receiptCurrency]. */
    val receiptAmount: String = "",
    /** Currency chips: receipt currencies used before, then currencies of the accounts. */
    val receiptCurrencies: List<String> = emptyList(),
    val receiptCurrencyError: ReceiptCurrencyError? = null,
    val receiptAmountError: AmountError? = null,
    val amountError: AmountError? = null,
    val toAmountError: AmountError? = null,
    val accountError: Boolean = false,
    val toAccountError: ToAccountError? = null,
    val categoryError: Boolean = false,
    /** Saved or deleted: the screen should close. */
    val isDone: Boolean = false,
    /** Which part of the two-step sheet is shown: a new operation starts on the account. */
    val step: EntryStep = EntryStep.DETAILS,
    /** Accounts of the latest operations, newest first (at most 4) — the top row of the picker. */
    val recentAccountIds: List<String> = emptyList(),
    /** Which amount the keypad types into. */
    val keypadTarget: KeypadTarget = KeypadTarget.AMOUNT,
    /** The template this new operation was filled from — the «⚡ Испанский ✕» pill; `null` — none. */
    val templateName: String? = null,
    /** «Сохранить как шаблон» succeeded: the dialog closes and the header shows the new template's name. */
    val savedTemplateName: String? = null,
    /** «Сохранить как шаблон» was refused: the operation's account or category is archived or gone. */
    val templateSaveFailed: Boolean = false,
) {
    data class AccountOption(
        val id: String,
        val name: String,
        val currency: String,
        val type: AccountType = AccountType.OTHER,
        val color: Long? = null,
        /** Formatted balance for the picker tile, `""` — unknown. */
        val balance: String = "",
        val fractionDigits: Int = 2,
    )

    /** Steps of the entry sheet (owner 2026-11-09: account first, then amount and category). */
    enum class EntryStep { ACCOUNT, TO_ACCOUNT, DETAILS }

    enum class KeypadTarget { AMOUNT, TO_AMOUNT, RECEIPT }

    /** Name of the chosen category (or subcategory), for the refund hint. */
    val categoryName: String?
        get() = categoryId?.let { id -> categories.firstNotNullOfOrNull { g -> (listOf(g.item) + g.children).firstOrNull { it.id == id } }?.name }

    data class CategoryItem(val id: String, val name: String, val icon: String? = null, val color: Long? = null)

    data class CategoryGroup(val item: CategoryItem, val children: List<CategoryItem>) {
        val isFolder: Boolean get() = children.isNotEmpty()
    }

    enum class AmountError { REQUIRED, INVALID, TOO_MANY_FRACTION_DIGITS, NOT_POSITIVE }
    enum class ToAccountError { REQUIRED, SAME_ACCOUNT }
    enum class ReceiptCurrencyError { INVALID, SAME_AS_ACCOUNT }

    val account: AccountOption? get() = accounts.firstOrNull { it.id == accountId }
    val toAccount: AccountOption? get() = accounts.firstOrNull { it.id == toAccountId }

    val isTransfer: Boolean get() = type == TransactionType.TRANSFER

    /** A transfer between currencies needs the arriving amount too. */
    val needsToAmount: Boolean
        get() = isTransfer && account != null && toAccount != null && account?.currency != toAccount?.currency

    /** The receipt block is shown and saved only for expenses and incomes. */
    val hasReceipt: Boolean get() = receiptEnabled && !isTransfer

    /** Chips without the account's own currency (a receipt in it makes no sense). */
    val receiptCurrencyChoices: List<String> get() = receiptCurrencies.filter { it != account?.currency }

    val isToday: Boolean get() = date == today
    val isYesterday: Boolean get() = date == today.minus(1, DateTimeUnit.DAY)

    /** [date] as UTC midnight millis — the format of Material's DatePicker. */
    val dateMillisUtc: Long get() = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

/** Where the sheet goes after an account is picked on [this] step. */
internal fun TransactionEditUiState.EntryStep.afterAccount(isTransfer: Boolean): TransactionEditUiState.EntryStep = when {
    this != TransactionEditUiState.EntryStep.ACCOUNT -> this
    isTransfer -> TransactionEditUiState.EntryStep.TO_ACCOUNT
    else -> TransactionEditUiState.EntryStep.DETAILS
}
