package app.coshell.core.domain.drafts

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
 * picks the account (a word of its name — «сбер», #632; the word «карта»/«нал», no word at all meaning cash; the currency the order of the number
 * suggests — «кофе 5» is dollars, [CurrencyGuess]; an account in the [Context.local] currency of the country the phone is in — #633; else the one used last) and the category ([CategoryGuess]: past entries, then the brand dictionary).
 * A line without a number becomes a draft without an amount («Нужна сумма») if something says it is a purchase; a line nothing
 * recognises is [isNoise] and stays in the note (#630). Pure code, rules only.
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

    /** Drafts of the lines that are not [isNoise], in order. */
    fun build(lines: List<String>, ctx: Context, ids: IdGenerator): List<Draft> =
        lines.filterNot { isNoise(it, ctx) }.map { line -> draft(line, ctx, ids.newId()) }

    /**
     * Not about money (#630): the line has no number and neither past entries nor the dictionary know what it is («тест заметки»,
     * «позвонить маме»). A line with a number is kept — it may be a purchase the person names their own way.
     */
    fun isNoise(line: String, ctx: Context): Boolean {
        if (read(line, ctx) != null) return false
        val title = titleOf(line, null, ctx)
        return suggest(title.ifBlank { line }, ctx) == null
    }

    private fun draft(line: String, ctx: Context, id: String): Draft {
        val note = read(line, ctx)
        val mention = AccountMention.find(titleOf(line, note, ctx), ctx.accounts.filter { !it.archived })
        val title = (mention?.title ?: titleOf(line, note, ctx)).trim()
        val named = note?.money?.currency.takeIf { note?.currencyFound == true }
        val byName = mention?.account?.takeIf { named == null || it.currency == named }
        // No currency typed and no account named: the order of the number says which currency it is (#635).
        val guessed = if (byName == null && named == null && note != null) {
            CurrencyGuess.byMagnitude(CurrencyGuess.wholeUnits(note.money.amountMinor, note.money.currency), ctx.accounts, ctx.history)
        } else {
            null
        }
        val account = byName ?: pickAccount(note?.account, named ?: guessed, ctx)
        val currency = note?.money?.currency?.takeIf { note.currencyFound } ?: account?.currency ?: ctx.fallback
        // A number read in the fallback currency is read again in the account's one (JPY has no cents).
        val amount = if (note != null && !note.currencyFound && currency != ctx.fallback) {
            QuickNoteParser.parse(line, currency, ctx.known)?.money?.amountMinor
        } else {
            note?.money?.amountMinor
        }
        val guess = suggest(title.ifBlank { line }, ctx)
        val (categoryId, source) = when {
            guess != null -> guess.categoryId to when (guess.source) {
                CategoryGuess.Source.HISTORY -> DraftCategorySource.HISTORY
                CategoryGuess.Source.DICTIONARY -> DraftCategorySource.DICTIONARY
            }
            else -> null to null
        }
        return Draft(
            id = id, title = title, currency = currency, amountMinor = amount, accountId = account?.id,
            categoryId = categoryId, categorySource = source, dateMillis = ctx.now - (note?.daysAgo ?: 0) * DAY_MS, createdAt = ctx.now,
        )
    }

    private fun read(line: String, ctx: Context): QuickNote? = QuickNoteParser.parse(line, ctx.fallback, ctx.known)

    /** The rules' title, or for a line without a number the line itself cleaned («купил хлеб» → «хлеб»). */
    private fun titleOf(line: String, note: QuickNote?, ctx: Context): String =
        if (note != null) note.title else QuickNoteParser.cleanTitle(line).ifBlank { line }

    private fun suggest(title: String, ctx: Context) =
        CategoryGuess.suggest(ctx.history, TransactionType.EXPENSE, ctx.categories, payee = title, note = title)

    /**
     * The account for a line: of the currency it names (if any; else the [Context.local] one, when there is such an account), of the
     * kind its word names — a line that says nothing about paying is cash (owner 2026-11-24: «кофе 3500» → наличные), if any account
     * is, the one used most recently; with no history the first in the list. `null` — nothing in the named currency, the person chooses.
     */
    internal fun pickAccount(word: AccountWord?, currency: CurrencyCode?, ctx: Context): Account? {
        val live = ctx.accounts.filter { !it.archived }
        val used = ctx.history.map { it.accountId }.distinct()
        val byUse = live.sortedBy { a -> used.indexOf(a.id).let { if (it < 0) Int.MAX_VALUE else it } }
        val kind = when (word) {
            AccountWord.CARD -> AccountType.CARD
            AccountWord.CASH, null -> AccountType.CASH
        }
        if (currency != null) {
            val named = byUse.filter { it.currency == currency }
            return named.firstOrNull { it.type == kind } ?: named.firstOrNull()
        }
        val local = ctx.local?.let { code -> byUse.filter { it.currency == code } }.orEmpty()
        if (word == null) {
            // Nothing said: cash, but only in the currency the line would get anyway — never dollars cash for a peso coffee.
            val usual = local.firstOrNull() ?: byUse.firstOrNull() ?: return null
            return byUse.firstOrNull { it.type == kind && it.currency == usual.currency } ?: usual
        }
        (local.firstOrNull { it.type == kind } ?: byUse.firstOrNull { it.type == kind })?.let { return it }
        return local.firstOrNull() ?: byUse.firstOrNull()
    }
}
