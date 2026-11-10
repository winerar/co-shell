package app.coshell.core.domain.importer.wallet

import app.coshell.core.domain.importer.ImportWrite
import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import kotlinx.datetime.TimeZone

/**
 * What the user chose in the wizard, still in Wallet terms (names, not ids).
 *
 * @property walletBalances «баланс в Wallet сейчас» by Wallet account name; the difference goes into
 *   the account's initial balance ([WalletBalances.reconcile]). Accounts not listed are left as is.
 */
data class WalletImportChoices(
    val accounts: Map<String, AccountChoice>,
    val categories: Map<WalletCategoryKey, CategoryChoice>,
    val unpaired: Map<Int, UnpairedChoice> = emptyMap(),
    val walletBalances: Map<String, Money> = emptyMap(),
    val timeZone: TimeZone,
)

/**
 * The preview and the exact write (stage 5c): nothing is saved until [write] goes to
 * `ImportRepository.save`.
 *
 * @property alreadyImported operations from the file that are stored already (same `importExternalId`).
 * @property balancesAfter balance of every touched or new account once [write] is saved, by account id.
 * @property accountNames co-shell account name by id, new accounts included — for the preview.
 * @property newAccounts how many of [ImportWrite.accounts] are new (the rest are reconciled existing ones).
 */
data class WalletImportPlan(
    val batchId: String,
    val write: ImportWrite,
    val alreadyImported: Int,
    val issues: List<WalletImportIssue>,
    val skipped: Int,
    val balancesAfter: Map<String, Money>,
    val accountNames: Map<String, String>,
    val newAccounts: Int,
) {
    val newCount: Int get() = write.transactions.size
}

/** Wallet records + wizard choices → [WalletImportPlan]. Pure: reads nothing, writes nothing. */
class WalletImportPlanner(private val ids: IdGenerator) {

    /**
     * @param existing live co-shell accounts with their current balances.
     * @param adjustmentCategories ids of «Корректировка баланса» by direction (must exist before saving).
     * @param known `importExternalId`s already stored for Wallet.
     */
    fun plan(
        records: List<WalletRecord>,
        choices: WalletImportChoices,
        existing: List<AccountWithBalance>,
        adjustmentCategories: Map<CategoryType, String>,
        known: Set<String>,
    ): WalletImportPlan {
        val existingById = existing.associateBy { it.account.id }
        val newAccounts = LinkedHashMap<Pair<String, String>, Account>()
        val accounts: Map<String, Account> = choices.accounts.mapNotNull { (walletName, choice) ->
            when (choice) {
                is AccountChoice.MergeInto -> existingById[choice.accountId]?.account
                is AccountChoice.CreateNew -> newAccounts.getOrPut(choice.name.trim().lowercase() to choice.currency.value) {
                    Account(ids.newId(), choice.name.trim(), choice.currency)
                }
            }?.let { walletName to it }
        }.toMap()

        val newCategories = LinkedHashMap<Pair<String, CategoryType>, Category>()
        val categories = choices.categories.mapValues { (_, choice) ->
            when (choice) {
                is CategoryChoice.UseExisting -> choice.categoryId
                is CategoryChoice.CreateNew -> newCategories.getOrPut(choice.name.trim().lowercase() to choice.type) {
                    Category(ids.newId(), choice.name.trim(), choice.type)
                }.id
            }
        }

        val batchId = ids.newId()
        val mapped = accounts.values.map { it.id }.toSet()
        val built = WalletImportBuilder(ids).build(
            records,
            WalletImportDecisions(
                accounts = accounts,
                categories = categories,
                adjustmentCategories = adjustmentCategories,
                baseCurrency = WalletBaseCurrency.detect(records),
                timeZone = choices.timeZone,
                unpaired = choices.unpaired,
                otherAccounts = existing.map { it.account }.filter { it.id !in mapped },
            ),
        )
        val fresh = built.transactions.filter { it.importExternalId !in known }.map { it.copy(importBatchId = batchId) }

        val touched = existing + newAccounts.values.map { AccountWithBalance(it, it.initialBalance) }
        val after = WalletBalances.afterImport(touched, fresh).toMutableMap()
        val reconciled = choices.walletBalances.mapNotNull { (walletName, walletBalance) ->
            val account = accounts[walletName] ?: return@mapNotNull null
            if (walletBalance.currency != account.currency) return@mapNotNull null
            val updated = WalletBalances.reconcile(account, after.getValue(account.id), walletBalance)
            after[account.id] = walletBalance
            updated.takeIf { it != account }
        }.associateBy { it.id }

        val accountsToSave = newAccounts.values.map { reconciled[it.id] ?: it } +
            reconciled.values.filter { it.id in existingById }
        val involved = fresh.flatMap { listOfNotNull(it.accountId, it.toAccountId) }.toSet() +
            accountsToSave.map { it.id }
        return WalletImportPlan(
            batchId = batchId,
            write = ImportWrite(accountsToSave, newCategories.values.toList(), fresh),
            alreadyImported = built.transactions.size - fresh.size,
            issues = built.issues,
            skipped = built.skipped,
            balancesAfter = after.filterKeys { it in involved },
            accountNames = touched.associate { it.account.id to it.account.name },
            newAccounts = newAccounts.size,
        )
    }
}
