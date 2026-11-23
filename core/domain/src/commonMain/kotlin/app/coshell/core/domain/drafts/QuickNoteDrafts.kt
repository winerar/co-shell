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
 * picks the account (a word of its name — «сбер», #632; the word «карта»/«нал»; an account in the [Context.local] currency of the
 * country the phone is in — #633; else the one used last) and the category ([CategoryGuess]: past entries, then the brand dictionary,
 * then the model's [NoteHint]). A line without a number becomes a draft without an amount («Нужна сумма») if something says it is
 * a purchase; a line nothing recognises, or one the model calls «not an expense», is [isNoise] and stays in the note (#630).
 * Pure code: the model is asked by the caller, one request for all the lines that need it ([needsModel]).
 */
object QuickNoteDrafts {
    private const val DAY_MS = 86_400_000L
    private val SPLIT = Regex("[\\n;]+|,\\s+")

    /**
     * What the lines are read against: the person's live [accounts], [categories], newest-first [history], the [now] of processing;
     * [local] — the currency of the country the phone is in (`null` when unknown), preferred when a line names none.
     */
    class Context(
        val accounts: List<Account>,
        val categories: List<Category>,
        val history: List<Transaction>,
        val fallback: CurrencyCode,
        val known: Set<CurrencyCode>,
        val now: Long,
        val local: CurrencyCode? = null,
    )

    /** One expense per line; several on a line go after «, » or «;» (a comma inside a number — «15,50» — has no space after it). */
    fun lines(text: String): List<String> = text.split(SPLIT).map { it.trim() }.filter { it.isNotEmpty() }

    /**
     * Lines the rules could not finish or whose category nothing could guess: only these go to the model. With a [broad] (cheap, cloud)
     * model also the lines whose way of paying the rules did not find, when there is a choice of accounts (#635).
     */
    fun needsModel(lines: List<String>, ctx: Context, broad: Boolean = false): List<Int> = lines.indices.filter { i ->
        val read = read(lines[i], ctx)
        read == null || suggest(read.title.ifBlank { lines[i] }, ctx) == null || (broad && accountUnsure(read, ctx))
    }

    /** No account word, no named currency and no account-name word in the line, but more than one account to choose from. */
    private fun accountUnsure(note: QuickNote, ctx: Context): Boolean {
        val live = ctx.accounts.filter { !it.archived }
        return live.size > 1 && note.account == null && !note.currencyFound && AccountMention.find(note.title, live) == null
    }

    /** Drafts of the lines that are not [isNoise], in order. */
    fun build(lines: List<String>, ctx: Context, ids: IdGenerator, hints: Map<Int, NoteHint> = emptyMap()): List<Draft> =
        lines.indices.filterNot { isNoise(lines[it], ctx, hints[it]) }.map { i -> draft(lines[i], ctx, ids.newId(), hints[i]) }

    /**
     * Not about money (#630): the model said «not an expense», or the line has no number and neither past entries, the dictionary
     * nor the model know what it is («тест заметки», «позвонить маме»). A line with a number is kept without a model — it may be a
     * purchase the person names their own way.
     */
    fun isNoise(line: String, ctx: Context, hint: NoteHint? = null): Boolean {
        if (hint?.expense == false) return true
        if (read(line, ctx) != null) return false
        val title = titleOf(line, null, ctx)
        return hint?.category == null && suggest(title.ifBlank { line }, ctx) == null
    }

    private fun draft(line: String, ctx: Context, id: String, hint: NoteHint?): Draft {
        val note = read(line, ctx)
        val mention = AccountMention.find(titleOf(line, note, ctx), ctx.accounts.filter { !it.archived })
        val title = (hint?.title ?: mention?.title ?: titleOf(line, note, ctx)).trim()
        val named = note?.money?.currency.takeIf { note?.currencyFound == true }
        val byHint = hint?.account?.let { AccountLabels.find(it, ctx.accounts.filter { a -> !a.archived }) }
            ?.takeIf { (named == null || it.currency == named) && kindFits(note?.account, it) }
        val account = mention?.account?.takeIf { named == null || it.currency == named } ?: byHint ?: pickAccount(note?.account, named, ctx)
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

    /** The model's account must agree with the word «карта»/«нал» of the line, if there is one. */
    private fun kindFits(word: AccountWord?, account: Account): Boolean = when (word) {
        AccountWord.CARD -> account.type == AccountType.CARD
        AccountWord.CASH -> account.type == AccountType.CASH
        null -> true
    }

    private fun read(line: String, ctx: Context): QuickNote? = QuickNoteParser.parse(line, ctx.fallback, ctx.known)

    /** The rules' title, or for a line without a number the line itself cleaned («купил хлеб» → «хлеб»). */
    private fun titleOf(line: String, note: QuickNote?, ctx: Context): String =
        if (note != null) note.title else QuickNoteParser.cleanTitle(line).ifBlank { line }

    private fun suggest(title: String, ctx: Context) =
        CategoryGuess.suggest(ctx.history, TransactionType.EXPENSE, ctx.categories, payee = title, note = title)

    /**
     * The account for a line: of the currency it names (if any; else the [Context.local] one, when there is such an account), of the
     * kind its word names (if any account is), the one used most recently; with no history the first in the list. `null` — nothing in
     * the named currency, the person chooses.
     */
    internal fun pickAccount(word: AccountWord?, currency: CurrencyCode?, ctx: Context): Account? {
        val live = ctx.accounts.filter { !it.archived }
        val used = ctx.history.map { it.accountId }.distinct()
        val byUse = live.sortedBy { a -> used.indexOf(a.id).let { if (it < 0) Int.MAX_VALUE else it } }
        val kind = when (word) {
            AccountWord.CARD -> AccountType.CARD
            AccountWord.CASH -> AccountType.CASH
            null -> null
        }
        if (currency != null) {
            val named = byUse.filter { it.currency == currency }
            return named.firstOrNull { it.type == kind } ?: named.firstOrNull()
        }
        val local = ctx.local?.let { code -> byUse.filter { it.currency == code } }.orEmpty()
        if (kind != null) (local.firstOrNull { it.type == kind } ?: byUse.firstOrNull { it.type == kind })?.let { return it }
        return local.firstOrNull() ?: byUse.firstOrNull()
    }
}
