package app.coshell.core.domain.drafts

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlin.time.Instant

/**
 * «Обработать» (#586): the note's text → drafts by rules, saved in one write; a line the rules could not finish waits as a draft
 * with the missing piece marked.
 */
class ProcessQuickNoteUseCase(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
    private val drafts: DraftRepository,
    private val ids: IdGenerator = IdGenerator.Random,
    /** The currency of the country the phone is in (by the mobile network, #633), `null` when unknown. */
    private val localCurrency: () -> CurrencyCode? = { null },
    private val now: () -> Long,
) {
    /** [made] drafts; [skipped] — lines that are not about money (#630), to be left in the note. */
    data class Result(val made: Int, val skipped: List<String>)

    /**
     * Drafts made and lines skipped; the text is not touched (the caller keeps only [Result.skipped] in the note). A number without a
     * currency is read in the currency of the account used last (else the first account, else USD); known currencies are the accounts'.
     */
    suspend operator fun invoke(text: String): Result {
        val lines = QuickNoteDrafts.lines(text)
        if (lines.isEmpty()) return Result(0, emptyList())
        val expense = categories.observeCategories(CategoryType.EXPENSE).first()
        val live = accounts.observeAccounts().first()
        val history = transactions.observeTransactions(TransactionFilter(limit = HISTORY)).first()
        val last = history.firstNotNullOfOrNull { t -> live.firstOrNull { it.id == t.accountId } } ?: live.firstOrNull()
        val ctx = QuickNoteDrafts.Context(
            accounts = live, categories = expense, history = history,
            fallback = last?.currency ?: CurrencyCode("USD"), known = live.mapTo(mutableSetOf()) { it.currency }, now = now(),
            local = localCurrency(),
        )
        val made = QuickNoteDrafts.build(lines, ctx, ids)
        drafts.saveDrafts(made)
        val skipped = lines.filter { QuickNoteDrafts.isNoise(it, ctx) }
        return Result(made.size, skipped)
    }

    private companion object {
        const val HISTORY = 500
    }
}

/**
 * «Записать N» (#586): posts every ready draft among [ids] as an expense and removes it; unready ones stay untouched.
 * [Result.transactionIds] let the caller offer «Отменить» (delete these operations; the drafts are gone, as with any delete).
 */
class PostDraftsUseCase(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val drafts: DraftRepository,
    private val save: SaveTransactionUseCase,
    private val ids: IdGenerator = IdGenerator.Random,
) {
    data class Result(val transactionIds: List<String>, val left: Int)

    suspend operator fun invoke(ids: List<String>): Result {
        val accountMap = accounts.observeAccounts().first().associateBy { it.id }
        val categoryMap = categories.observeCategories(includeArchived = true).first().associateBy { it.id }
        val posted = mutableListOf<String>()
        val done = mutableListOf<String>()
        for (id in ids) {
            val draft = drafts.getDraft(id) ?: continue
            if (DraftCheck.missing(draft, accountMap, categoryMap) != null) continue
            val transaction = Transaction(
                id = this.ids.newId(), type = TransactionType.EXPENSE, accountId = draft.accountId!!,
                amount = Money(draft.amountMinor!!, draft.currency), date = Instant.fromEpochMilliseconds(draft.dateMillis),
                categoryId = draft.categoryId, note = draft.title,
            )
            if (save(transaction) is SaveTransactionUseCase.Result.Saved) {
                posted += transaction.id
                done += id
            }
        }
        drafts.deleteDrafts(done)
        return Result(posted, ids.size - done.size)
    }
}
