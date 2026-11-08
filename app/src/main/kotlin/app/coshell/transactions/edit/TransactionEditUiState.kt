package app.coshell.transactions.edit

import app.coshell.core.model.TransactionType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus

data class TransactionEditUiState(
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    val type: TransactionType = TransactionType.EXPENSE,
    /** Amount as typed, in the currency of [accountId]. */
    val amount: String = "",
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
) {
    data class AccountOption(val id: String, val name: String, val currency: String, val currencySymbol: String)

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
