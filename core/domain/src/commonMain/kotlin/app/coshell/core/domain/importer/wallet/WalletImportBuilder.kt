package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.Account
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/** What to do with a transfer record whose second half is not in the file. */
sealed interface UnpairedChoice {
    /** Default: an expense/income in the «balance adjustment» category (owner: such packs were balance fixes). */
    data object Adjustment : UnpairedChoice

    /** A transfer with this co-shell account as the other side. */
    data class TransferWith(val accountId: String) : UnpairedChoice

    data object Skip : UnpairedChoice
}

/**
 * The wizard's decisions, already resolved to co-shell ids: new accounts and categories are created
 * before building (so their ids are known).
 *
 * @property accounts Wallet account name → co-shell account.
 * @property categories Wallet category + direction → co-shell category id.
 * @property adjustmentCategories category ids for [UnpairedChoice.Adjustment], by direction.
 * @property baseCurrency Wallet's base currency ([WalletBaseCurrency]) — converts records in a foreign currency.
 */
data class WalletImportDecisions(
    val accounts: Map<String, Account>,
    val categories: Map<WalletCategoryKey, String>,
    val adjustmentCategories: Map<CategoryType, String>,
    val baseCurrency: CurrencyCode?,
    val timeZone: TimeZone,
    val unpaired: Map<Int, UnpairedChoice> = emptyMap(),
)

/** Why a record was not turned into a transaction. */
enum class WalletImportProblem {
    /** Its account or category has no decision. */
    UNMAPPED,

    /** Amount in a currency other than the account's, and the account is not in Wallet's base currency. */
    NEEDS_RATE,
    ZERO_AMOUNT,
}

data class WalletImportIssue(val line: Int, val problem: WalletImportProblem)

data class WalletImportResult(val transactions: List<Transaction>, val issues: List<WalletImportIssue>, val skipped: Int)

/** Turns parsed Wallet records into co-shell transactions (stage 5b; ARCHITECTURE §8). */
class WalletImportBuilder(private val ids: IdGenerator) {

    fun build(records: List<WalletRecord>, decisions: WalletImportDecisions): WalletImportResult {
        val externalIds = WalletExternalIds.of(records)
        val transfers = WalletTransferMatcher.match(records)
        val out = ArrayList<Transaction>(records.size)
        val issues = mutableListOf<WalletImportIssue>()
        var skipped = 0
        val ctx = Context(decisions, externalIds, issues)

        records.filter { !it.isTransfer }.forEach { r -> ctx.single(r)?.let(out::add) }
        transfers.pairs.forEach { p -> ctx.pair(p)?.let(out::add) }
        transfers.unpaired.forEach { r ->
            when (val choice = decisions.unpaired[r.line] ?: UnpairedChoice.Adjustment) {
                UnpairedChoice.Skip -> skipped++
                UnpairedChoice.Adjustment -> ctx.adjustment(r)?.let(out::add)
                is UnpairedChoice.TransferWith -> ctx.transferWith(r, choice.accountId)?.let(out::add)
            }
        }
        return WalletImportResult(out.sortedByDescending { it.date }, issues.sortedBy { it.line }, skipped)
    }

    private inner class Context(
        val d: WalletImportDecisions,
        val externalIds: Map<WalletRecord, String>,
        val issues: MutableList<WalletImportIssue>,
    ) {
        fun single(r: WalletRecord): Transaction? {
            val account = d.accounts[r.account] ?: return issue(r, WalletImportProblem.UNMAPPED)
            val categoryId = d.categories[r.categoryKey()] ?: return issue(r, WalletImportProblem.UNMAPPED)
            return plain(r, account, categoryId)
        }

        fun adjustment(r: WalletRecord): Transaction? {
            val account = d.accounts[r.account] ?: return issue(r, WalletImportProblem.UNMAPPED)
            val type = if (r.amount.isNegative) CategoryType.EXPENSE else CategoryType.INCOME
            val categoryId = d.adjustmentCategories[type] ?: return issue(r, WalletImportProblem.UNMAPPED)
            return plain(r, account, categoryId)
        }

        fun pair(p: WalletTransferPair): Transaction? {
            val from = d.accounts[p.from.account] ?: return issue(p.from, WalletImportProblem.UNMAPPED)
            val to = d.accounts[p.to.account] ?: return issue(p.to, WalletImportProblem.UNMAPPED)
            if (from.id == to.id) return issue(p.from, WalletImportProblem.UNMAPPED) // both merged into one account
            val amount = inAccountCurrency(p.from, from) ?: return null
            val toAmount = inAccountCurrency(p.to, to) ?: return null
            return Transaction(
                id = ids.newId(), type = TransactionType.TRANSFER, accountId = from.id, amount = amount,
                date = p.from.dateTime.toInstant(d.timeZone), toAccountId = to.id, toAmount = toAmount,
                note = listOf(p.from.note, p.to.note).filter { it.isNotBlank() }.distinct().joinToString(" · "),
                labels = listOfNotNull(p.from.label ?: p.to.label),
                importSource = SOURCE, importExternalId = WalletExternalIds.pair(externalIds.getValue(p.from), externalIds.getValue(p.to)),
            )
        }

        /** The record is one side; [otherId] is the other. A negative record leaves its account. */
        fun transferWith(r: WalletRecord, otherId: String): Transaction? {
            val own = d.accounts[r.account] ?: return issue(r, WalletImportProblem.UNMAPPED)
            val other = d.accounts.values.firstOrNull { it.id == otherId } ?: return issue(r, WalletImportProblem.UNMAPPED)
            if (own.id == other.id) return issue(r, WalletImportProblem.UNMAPPED)
            val ownAmount = inAccountCurrency(r, own) ?: return null
            val otherAmount = inAccountCurrency(r, other) ?: return null
            val leaves = r.amount.isNegative
            return Transaction(
                id = ids.newId(), type = TransactionType.TRANSFER,
                accountId = if (leaves) own.id else other.id, amount = if (leaves) ownAmount else otherAmount,
                toAccountId = if (leaves) other.id else own.id, toAmount = if (leaves) otherAmount else ownAmount,
                date = r.dateTime.toInstant(d.timeZone), note = r.note, labels = listOfNotNull(r.label),
                importSource = SOURCE, importExternalId = externalIds.getValue(r),
            )
        }

        private fun plain(r: WalletRecord, account: Account, categoryId: String): Transaction? {
            val amount = inAccountCurrency(r, account) ?: return null
            return Transaction(
                id = ids.newId(),
                type = if (r.amount.isNegative) TransactionType.EXPENSE else TransactionType.INCOME,
                accountId = account.id, amount = amount, date = r.dateTime.toInstant(d.timeZone), categoryId = categoryId,
                note = r.note, payee = r.payee, labels = listOfNotNull(r.label),
                originalAmount = r.amount.abs().takeIf { it.currency != account.currency },
                importSource = SOURCE, importExternalId = externalIds.getValue(r),
            )
        }

        /**
         * Positive amount in [account]'s currency: as is, or from `ref_currency_amount` when the
         * account is in Wallet's base currency; otherwise a rate is needed (reported, not guessed).
         */
        private fun inAccountCurrency(r: WalletRecord, account: Account): Money? {
            if (r.amount.isZero) return issue(r, WalletImportProblem.ZERO_AMOUNT)
            if (r.amount.currency == account.currency) return r.amount.abs()
            if (account.currency != d.baseCurrency) return issue(r, WalletImportProblem.NEEDS_RATE)
            return r.refAmount.abs().toMoney(account.currency)?.takeIf { it.isPositive }
                ?: issue(r, WalletImportProblem.NEEDS_RATE)
        }

        private fun issue(r: WalletRecord, problem: WalletImportProblem): Nothing? {
            issues += WalletImportIssue(r.line, problem)
            return null
        }
    }

    companion object {
        const val SOURCE = "wallet"
    }
}
