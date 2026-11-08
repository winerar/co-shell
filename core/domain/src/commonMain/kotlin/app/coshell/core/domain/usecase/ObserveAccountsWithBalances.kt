package app.coshell.core.domain.usecase

import app.coshell.core.domain.AccountWithBalance
import app.coshell.core.domain.balanceOf
import app.coshell.core.domain.repository.AccountRepository
import app.coshell.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Live list of accounts with their current balances, each in the account's own currency. */
class ObserveAccountsWithBalances(
    private val accounts: AccountRepository,
    private val transactions: TransactionRepository,
) {
    operator fun invoke(includeArchived: Boolean = false): Flow<List<AccountWithBalance>> =
        combine(
            accounts.observeAccounts(includeArchived),
            transactions.observeTransactions(),
        ) { accountList, txList ->
            accountList.map { AccountWithBalance(it, balanceOf(it, txList)) }
        }
}
