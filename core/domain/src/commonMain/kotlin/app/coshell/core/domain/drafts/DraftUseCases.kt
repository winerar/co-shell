package app.coshell.core.domain.drafts

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.ai.NoteHint
import app.coshell.core.domain.ai.NoteHints
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlin.time.Instant

/**
 * «Обработать» (#586): the note's text → drafts, saved in one write. The model (if installed) reads only the lines rules could not
 * finish, all in one request; without it those lines wait as drafts with the missing piece marked.
 */
class ProcessQuickNoteUseCase(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
    private val drafts: DraftRepository,
    private val hints: NoteHints?,
    private val ids: IdGenerator = IdGenerator.Random,
    private val now: () -> Long,
) {
    /**
     * The number of drafts made; the text is not touched (the caller clears the note once this returns). A number without a
     * currency is read in the currency of the account used last (else the first account, else USD); known currencies are the accounts'.
     */
    suspend operator fun invoke(text: String): Int {
        val lines = QuickNoteDrafts.lines(text)
        if (lines.isEmpty()) return 0
        val expense = categories.observeCategories(CategoryType.EXPENSE).first()
        val live = accounts.observeAccounts().first()
        val history = transactions.observeTransactions(TransactionFilter(limit = HISTORY)).first()
        val last = history.firstNotNullOfOrNull { t -> live.firstOrNull { it.id == t.accountId } } ?: live.firstOrNull()
        val ctx = QuickNoteDrafts.Context(
            accounts = live, categories = expense, history = history,
            fallback = last?.currency ?: CurrencyCode("USD"), known = live.mapTo(mutableSetOf()) { it.currency }, now = now(),
        )
        val ask = hints?.let { QuickNoteDrafts.needsModel(lines, ctx) }.orEmpty()
        val read: List<NoteHint?> = hints?.takeIf { ask.isNotEmpty() }?.read(ask.map { lines[it] }, expense.map { it.name }).orEmpty()
        val byLine = ask.zip(read).mapNotNull { (i, hint) -> hint?.let { i to it } }.toMap()
        drafts.saveDrafts(QuickNoteDrafts.build(lines, ctx, ids, byLine))
        return lines.size
    }

    private companion object {
        const val HISTORY = 200
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
