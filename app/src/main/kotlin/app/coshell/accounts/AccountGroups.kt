package app.coshell.accounts

import androidx.annotation.StringRes
import app.coshell.R
import app.coshell.core.model.AccountType

/** Order of account groups: what is paid with most often first. Used by the accounts list and the picker. */
val AccountTypeGroupOrder: List<AccountType> =
    listOf(AccountType.CARD, AccountType.CRYPTO, AccountType.CASH, AccountType.DEPOSIT, AccountType.OTHER)

/** [items] split by type in [AccountTypeGroupOrder], empty groups dropped, order inside a group kept. */
fun <T> groupByAccountType(items: List<T>, typeOf: (T) -> AccountType): List<Pair<AccountType, List<T>>> {
    val byType = items.groupBy(typeOf)
    return AccountTypeGroupOrder.mapNotNull { type -> byType[type]?.let { type to it } }
}

/** Group title (plural): «Карты и банки», «Наличные»… */
@get:StringRes
val AccountType.groupLabelRes: Int
    get() = when (this) {
        AccountType.CARD -> R.string.account_group_card
        AccountType.CRYPTO -> R.string.account_group_crypto
        AccountType.CASH -> R.string.account_group_cash
        AccountType.DEPOSIT -> R.string.account_group_deposit
        AccountType.OTHER -> R.string.account_group_other
    }
