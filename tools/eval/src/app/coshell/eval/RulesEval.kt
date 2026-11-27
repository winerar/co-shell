package app.coshell.eval

import app.coshell.core.domain.DefaultCategories
import app.coshell.core.domain.categoryguess.CategoryGuess
import app.coshell.core.domain.drafts.QuickNoteDrafts
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.time.Instant

/**
 * Runs the gold set through **the app's own code** (#742): quick notes go through the rules exactly as «Обработать» does, a payee the
 * person types for the first time through the family's history and the brand dictionary, as the entry sheet does. Each miss says which
 * field was wrong, so a change of the rules or the dictionary can be measured before it ships.
 */
class RulesEval(private val gold: EvalGold) {
    /** [misses] — one line per wrong case saying which field was wrong. */
    class Score(val name: String, val ok: Int, val of: Int, val misses: List<String>) {
        val short: String get() = "$name $ok/$of"
    }

    /** The starter categories a new person gets (with their icons: the brand dictionary finds a category by its icon). */
    val categories: List<Category> = run {
        var n = 0
        DefaultCategories.create({ "c${n++}" }).filter { it.type == CategoryType.EXPENSE }
    }
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val accounts = listOf(
        Account("card", "Карта", ars, AccountType.CARD),
        Account("cash", "Наличные", ars, AccountType.CASH),
        Account("usd", "Карта USD", usd, AccountType.CARD),
        Account("usd-cash", "Доллары наличные", usd, AccountType.CASH),
    )

    /** The family's past: every gold payee once — what the app would have learnt from a few months of records. */
    private fun history(except: String? = null): List<Transaction> = gold.payees.filter { it.payee != except }.mapIndexed { i, p ->
        Transaction(
            id = "h$i", type = TransactionType.EXPENSE, accountId = "card", amount = Money(100_00, ars), date = Instant.fromEpochMilliseconds(NOW - DAY_MS * (i + 1)),
            categoryId = categories.first { it.name == p.category }.id, payee = p.payee,
        )
    }

    /** All measures in a fixed order. */
    fun all(): List<Score> = listOf(notes(false), notes(true), payees(false), payees(true))

    /** Every note line; [withHistory] — the family has records (the rules learn from them). */
    fun notes(withHistory: Boolean): Score {
        val ctx = QuickNoteDrafts.Context(accounts, categories, if (withHistory) history() else emptyList(), fallback = ars, known = setOf(ars, usd), now = NOW)
        var ok = 0
        val misses = mutableListOf<String>()
        gold.notes.forEachIndexed { i, n ->
            val draft = QuickNoteDrafts.build(listOf(n.line), ctx, { "d$i" }).firstOrNull()
            val wrong = buildList {
                if (n.noise) { if (draft != null) add("записана как трата"); return@buildList }
                if (draft == null) { add("пропущена как «не трата»"); return@buildList }
                if (draft.amountMinor != n.amountMinor) add("сумма ${draft.amountMinor} ≠ ${n.amountMinor}")
                if (!draft.title.lowercase().contains(n.title.substringBefore(' ').lowercase())) add("название «${draft.title}» ≠ «${n.title}»")
                val day = draft.dateMillis.floorDiv(DAY_MS) - NOW.floorDiv(DAY_MS)
                if (day != n.day.toLong()) add("день $day ≠ ${n.day}")
                val category = categories.firstOrNull { it.id == draft.categoryId }?.name
                if (n.categories.isNotEmpty() && category !in n.categories) add("категория ${category ?: "—"} ≠ ${n.categories.joinToString(" / ")}")
                val type = accounts.firstOrNull { it.id == draft.accountId }?.type
                if (n.pay == "cash" && type != AccountType.CASH) add("счёт не наличные")
                if (n.pay == "card" && type != AccountType.CARD) add("счёт не карта")
            }
            if (wrong.isEmpty()) ok++ else misses += "«${n.line}»: ${wrong.joinToString("; ")}"
        }
        return Score(if (withHistory) "заметки (с историей)" else "заметки (новый человек)", ok, gold.notes.size, misses)
    }

    /** A payee the person types for the first time: history (without this payee) and the brand dictionary, the order of the entry sheet. */
    fun payees(withHistory: Boolean): Score {
        var ok = 0
        val misses = mutableListOf<String>()
        for (p in gold.payees) {
            val history = if (withHistory) history(except = p.payee) else emptyList()
            val got = CategoryGuess.suggest(history, TransactionType.EXPENSE, categories, payee = p.payee)?.let { s -> categories.first { it.id == s.categoryId }.name }
            if (got in p.categories) ok++ else misses += "«${p.payee}»: ${got ?: "—"} ≠ ${p.category}"
        }
        return Score(if (withHistory) "категория (с историей)" else "категория (новый человек)", ok, gold.payees.size, misses)
    }

    companion object {
        const val NOW = 1_795_273_200_000L // 2026-11-21T15:00:00Z, «today» of the gold set
        const val DAY_MS = 86_400_000L
    }
}
