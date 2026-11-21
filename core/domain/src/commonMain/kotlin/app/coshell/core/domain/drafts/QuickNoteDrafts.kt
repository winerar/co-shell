package app.coshell.core.domain.drafts

import app.coshell.core.domain.ai.NoteHint
import app.coshell.core.domain.categoryguess.CategoryGuess
import app.coshell.core.domain.quickentry.AccountWord
import app.coshell.core.domain.quickentry.QuickNote
import app.coshell.core.domain.quickentry.QuickNoteParser
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Draft
import app.coshell.core.model.DraftCategorySource
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType

/**
 * Turns the text of a quick note into drafts (#586): [lines] splits it, [build] reads each line by rules ([QuickNoteParser]),
 * picks the account (the word «карта»/«наличные», else the one used last) and the category ([CategoryGuess]: past entries, then the
 * brand dictionary, then the model's [NoteHint]). A line without a number becomes a draft without an amount («Нужна сумма»).
 * Pure code: the model is asked by the caller, one request for all the lines that need it ([needsModel]).
 */
object QuickNoteDrafts {
    private const val DAY_MS = 86_400_000L
    private val SPLIT = Regex("[\\n;]+|,\\s+")

    /** What the lines are read against: the person's live [accounts], [categories], newest-first [history], the [now] of processing. */
    class Context(
        val accounts: List<Account>,
        val categories: List<Category>,
        val history: List<Transaction>,
        val fallback: CurrencyCode,
        val known: Set<CurrencyCode>,
        val now: Long,
    )

    /** One expense per line; several on a line go after «, » or «;» (a comma inside a number — «15,50» — has no space after it). */
    fun lines(text: String): List<String> = text.split(SPLIT).map { it.trim() }.filter { it.isNotEmpty() }

    /** Lines the rules could not finish or whose category nothing could guess: only these go to the model. */
    fun needsModel(lines: List<String>, ctx: Context): List<Int> = lines.indices.filter { i ->
        val read = read(lines[i], ctx)
        read == null || suggest(read.title.ifBlank { lines[i] }, ctx) == null
    }

    fun build(lines: List<String>, ctx: Context, ids: IdGenerator, hints: Map<Int, NoteHint> = emptyMap()): List<Draft> =
        lines.mapIndexed { i, line -> draft(line, ctx, ids.newId(), hints[i]) }

    private fun draft(line: String, ctx: Context, id: String, hint: NoteHint?): Draft {
        val note = read(line, ctx)
        val title = (hint?.title ?: note?.title?.takeIf { it.isNotBlank() } ?: if (note == null) line else "").trim()
        val account = pickAccount(note?.account, note?.money?.currency.takeIf { note?.currencyFound == true }, ctx)
        val currency = note?.money?.currency?.takeIf { note.currencyFound } ?: account?.currency ?: ctx.fallback
        // A number read in the fallback currency is read again in the account's one (JPY has no cents).
        val amount = if (note != null && !note.currencyFound && currency != ctx.fallback) {
            QuickNoteParser.parse(line, currency, ctx.known)?.money?.amountMinor
        } else {
            note?.money?.amountMinor
        }
        val guess = suggest(title.ifBlank { line }, ctx)
        val byModel = hint?.category?.let { name -> ctx.categories.firstOrNull { it.type == CategoryType.EXPENSE && !it.archived && it.name == name } }
        val (categoryId, source) = when {
            guess != null -> guess.categoryId to when (guess.source) {
                CategoryGuess.Source.HISTORY -> DraftCategorySource.HISTORY
                CategoryGuess.Source.DICTIONARY -> DraftCategorySource.DICTIONARY
            }
            byModel != null -> byModel.id to DraftCategorySource.AI
            else -> null to null
        }
        return Draft(
            id = id, title = title, currency = currency, amountMinor = amount, accountId = account?.id,
            categoryId = categoryId, categorySource = source, dateMillis = ctx.now - (note?.daysAgo ?: 0) * DAY_MS, createdAt = ctx.now,
        )
    }

    private fun read(line: String, ctx: Context): QuickNote? = QuickNoteParser.parse(line, ctx.fallback, ctx.known)

    private fun suggest(title: String, ctx: Context) =
        CategoryGuess.suggest(ctx.history, TransactionType.EXPENSE, ctx.categories, payee = title, note = title)

    /**
     * The account for a line: of the currency it names (if any), of the kind its word names (if any account is), the one used most
     * recently; with no history the first in the list. `null` — nothing in that currency, the person chooses.
     */
    internal fun pickAccount(word: AccountWord?, currency: CurrencyCode?, ctx: Context): Account? {
        val live = ctx.accounts.filter { !it.archived }
        val used = ctx.history.map { it.accountId }.distinct()
        val byUse = live.sortedBy { a -> used.indexOf(a.id).let { if (it < 0) Int.MAX_VALUE else it } }
        val inCurrency = if (currency == null) byUse else byUse.filter { it.currency == currency }
        val kind = when (word) {
            AccountWord.CARD -> AccountType.CARD
            AccountWord.CASH -> AccountType.CASH
            null -> null
        }
        return inCurrency.firstOrNull { kind != null && it.type == kind } ?: inCurrency.firstOrNull()
    }
}
