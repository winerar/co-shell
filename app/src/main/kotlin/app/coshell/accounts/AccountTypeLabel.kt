package app.coshell.accounts

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.coshell.R
import app.coshell.core.model.AccountType
import app.coshell.core.designsystem.R as DesignR

@get:StringRes
val AccountType.labelRes: Int
    get() = when (this) {
        AccountType.CASH -> R.string.account_type_cash
        AccountType.CARD -> R.string.account_type_card
        AccountType.DEPOSIT -> R.string.account_type_deposit
        AccountType.CRYPTO -> R.string.account_type_crypto
        AccountType.OTHER -> R.string.account_type_other
    }

/** Icon of an account type in its badge (`IconBadge`, tinted with the account's colour). */
@get:DrawableRes
val AccountType.iconRes: Int
    get() = when (this) {
        AccountType.CASH -> DesignR.drawable.ic_payments
        AccountType.CARD -> DesignR.drawable.ic_credit_card
        AccountType.DEPOSIT -> DesignR.drawable.ic_savings
        AccountType.CRYPTO -> DesignR.drawable.ic_currency_exchange
        AccountType.OTHER -> DesignR.drawable.ic_account_balance_wallet
    }
