package app.coshell.core.domain.drafts

import app.coshell.core.model.Account
import app.coshell.core.model.AccountType

/**
 * How an account is named to the model (#635): «Name · CUR · kind» — unique enough to tell «Galicia ARS» from «Galicia USD» and a card
 * from cash. The answer is taken back by [find]; a label the model made up finds nothing.
 */
object AccountLabels {
    fun of(account: Account): String = "${account.name} · ${account.currency.value} · ${kind(account.type)}"

    /** The account whose [label] this is (case-insensitive), `null` when none or when two have the same label. */
    fun find(label: String, accounts: List<Account>): Account? =
        accounts.filter { of(it).equals(label.trim(), ignoreCase = true) }.singleOrNull()

    private fun kind(type: AccountType) = type.name.lowercase()
}
