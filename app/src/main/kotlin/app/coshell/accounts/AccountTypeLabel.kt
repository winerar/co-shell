package app.coshell.accounts

import androidx.annotation.StringRes
import app.coshell.R
import app.coshell.core.model.AccountType

@get:StringRes
val AccountType.labelRes: Int
    get() = when (this) {
        AccountType.CASH -> R.string.account_type_cash
        AccountType.CARD -> R.string.account_type_card
        AccountType.DEPOSIT -> R.string.account_type_deposit
        AccountType.CRYPTO -> R.string.account_type_crypto
        AccountType.OTHER -> R.string.account_type_other
    }
