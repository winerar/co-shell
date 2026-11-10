package app.coshell.importer.wallet

import app.coshell.core.domain.importer.wallet.AccountChoice
import app.coshell.core.domain.importer.wallet.CategoryChoice
import app.coshell.core.domain.importer.wallet.UnpairedChoice
import app.coshell.core.domain.importer.wallet.WalletAccountInfo
import app.coshell.core.domain.importer.wallet.WalletAccountMatcher
import app.coshell.core.domain.importer.wallet.WalletCategoryKey
import app.coshell.core.domain.importer.wallet.WalletCategoryMatcher
import app.coshell.core.domain.importer.wallet.WalletImportChoices
import app.coshell.core.domain.importer.wallet.WalletRecord
import app.coshell.core.domain.importer.wallet.WalletSummary
import app.coshell.core.domain.importer.wallet.WalletTransferMatcher
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyParser
import kotlinx.datetime.TimeZone

/**
 * The wizard's working copy (stage 5c): what the file holds and what the user chose so far.
 * Pure — [WalletImportViewModel] keeps it in its state and asks it for [choices].
 *
 * @property balanceTexts «баланс в Wallet сейчас» as typed, by Wallet account name; blank — not reconciled.
 */
data class WalletImportDraft(
    val records: List<WalletRecord>,
    val summary: WalletSummary,
    val accounts: List<WalletAccountInfo>,
    val accountChoices: Map<String, AccountChoice>,
    val balanceTexts: Map<String, String> = emptyMap(),
    val categoryCounts: Map<WalletCategoryKey, Int>,
    val categoryChoices: Map<WalletCategoryKey, CategoryChoice>,
    val unpaired: List<WalletRecord>,
    val unpairedChoices: Map<Int, UnpairedChoice> = emptyMap(),
) {
    val categoryKeys: List<WalletCategoryKey> get() = categoryCounts.keys.toList()

    fun chooseAccount(walletName: String, choice: AccountChoice) = copy(accountChoices = accountChoices + (walletName to choice))

    fun typeBalance(walletName: String, text: String) = copy(balanceTexts = balanceTexts + (walletName to text))

    fun chooseCategory(key: WalletCategoryKey, choice: CategoryChoice) = copy(categoryChoices = categoryChoices + (key to choice))

    fun chooseUnpaired(line: Int, choice: UnpairedChoice) = copy(unpairedChoices = unpairedChoices + (line to choice))

    /** A typed balance that is not a valid amount in the account's currency (blank is fine). */
    fun balanceInvalid(walletName: String): Boolean {
        val text = balanceTexts[walletName].orEmpty()
        return text.isNotBlank() && parsedBalance(walletName) == null
    }

    val hasInvalidBalance: Boolean get() = accounts.any { balanceInvalid(it.name) }

    fun choices(timeZone: TimeZone) = WalletImportChoices(
        accounts = accountChoices,
        categories = categoryChoices,
        unpaired = unpairedChoices,
        walletBalances = accounts.mapNotNull { info -> parsedBalance(info.name)?.let { info.name to it } }.toMap(),
        timeZone = timeZone,
    )

    private fun parsedBalance(walletName: String): Money? {
        val info = accounts.firstOrNull { it.name == walletName } ?: return null
        val text = balanceTexts[walletName]?.takeIf { it.isNotBlank() } ?: return null
        return MoneyParser.parseOrNull(text, info.currency)
    }

    companion object {
        /** Suggestions first: same-named accounts and categories are reused, the rest is created. */
        fun start(records: List<WalletRecord>, existingAccounts: List<Account>, existingCategories: List<Category>): WalletImportDraft {
            val accounts = WalletAccountMatcher.accounts(records)
            val counts = WalletCategoryMatcher.counts(records)
            return WalletImportDraft(
                records = records,
                summary = WalletSummary.of(records),
                accounts = accounts,
                accountChoices = WalletAccountMatcher.suggest(accounts, existingAccounts),
                categoryCounts = counts,
                categoryChoices = WalletCategoryMatcher.suggest(counts.keys.toList(), existingCategories),
                unpaired = WalletTransferMatcher.match(records).unpaired.sortedByDescending { it.dateTime },
            )
        }
    }
}
