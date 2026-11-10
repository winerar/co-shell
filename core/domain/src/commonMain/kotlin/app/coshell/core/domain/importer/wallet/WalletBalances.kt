package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction

/**
 * Balance reconciliation (stage 5b; ARCHITECTURE §8): the CSV has no starting balances, so after
 * the import an account's balance differs from Wallet's. The user types «баланс в Wallet сейчас»,
 * and the difference goes into the account's initial balance — no extra operations.
 */
object WalletBalances {

    /**
     * Balance of each account once [imported] is saved: its current balance plus what the new
     * operations move. Accounts the import does not touch keep their balance.
     */
    fun afterImport(accounts: List<AccountWithBalance>, imported: List<Transaction>): Map<String, Money> =
        accounts.associate { (account, balance) ->
            account.id to imported.fold(balance) { sum, t -> t.effectOn(account.id)?.let(sum::plus) ?: sum }
        }

    /**
     * [account] with the initial balance shifted so that its balance becomes [walletBalance];
     * [balanceAfterImport] comes from [afterImport]. Both amounts must be in the account's currency.
     */
    fun reconcile(account: Account, balanceAfterImport: Money, walletBalance: Money): Account {
        require(balanceAfterImport.currency == account.currency && walletBalance.currency == account.currency) {
            "Balances must be in the account currency ${account.currency}"
        }
        val difference = walletBalance - balanceAfterImport
        if (difference.isZero) return account
        return account.copy(initialBalanceMinor = (account.initialBalance + difference).amountMinor)
    }
}
