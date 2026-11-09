package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode

/** A Wallet account as seen in the file. [currency] — the most frequent currency of its records. */
data class WalletAccountInfo(val name: String, val currency: CurrencyCode, val records: Int)

/** What to do with a Wallet account (stage 5b). */
sealed interface AccountChoice {
    /** A new co-shell account with the Wallet name and currency, initial balance 0. */
    data class CreateNew(val name: String, val currency: CurrencyCode) : AccountChoice

    /** Merge into an existing co-shell account — only one with the same currency. */
    data class MergeInto(val accountId: String) : AccountChoice
}

object WalletAccountMatcher {

    /**
     * Wallet accounts in file order. A record may be in another currency than its account
     * (one RUB transfer on an AMD account in the owner's export) — the majority wins.
     */
    fun accounts(records: List<WalletRecord>): List<WalletAccountInfo> =
        records.groupBy { it.account }.map { (name, rows) ->
            val currency = rows.groupingBy { it.amount.currency }.eachCount().maxBy { it.value }.key
            WalletAccountInfo(name, currency, rows.size)
        }

    /** Suggestion: merge into a live account with the same name (ignoring case) and currency, else create. */
    fun suggest(accounts: List<WalletAccountInfo>, existing: List<Account>): Map<String, AccountChoice> =
        accounts.associate { info ->
            val match = existing.firstOrNull {
                !it.archived && it.currency == info.currency && it.name.trim().equals(info.name.trim(), ignoreCase = true)
            }
            info.name to (match?.let { AccountChoice.MergeInto(it.id) } ?: AccountChoice.CreateNew(info.name, info.currency))
        }

    /** Merging is only allowed into an account of the same currency. */
    fun canMerge(info: WalletAccountInfo, target: Account): Boolean = info.currency == target.currency
}
