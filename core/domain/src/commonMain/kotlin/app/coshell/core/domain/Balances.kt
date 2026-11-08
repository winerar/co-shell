package app.coshell.core.domain

import app.coshell.core.model.Account
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction

/** Current balance of [account]: initial balance plus the effect of every live transaction. */
fun balanceOf(account: Account, transactions: Iterable<Transaction>): Money =
    transactions.fold(account.initialBalance) { acc, tx ->
        acc + Money(tx.effectOn(account.id), account.currency)
    }

data class AccountWithBalance(val account: Account, val balance: Money)
