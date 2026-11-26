package app.coshell.aieval

import app.coshell.core.domain.DefaultCategories
import app.coshell.core.domain.ai.CategoryHints
import app.coshell.core.domain.ai.HistoryExamples
import app.coshell.core.domain.ai.LanguageModel
import app.coshell.core.domain.ai.NoteHint
import app.coshell.core.domain.ai.NoteHints
import app.coshell.core.domain.ai.PayeeCategoryAi
import app.coshell.core.domain.ai.ReceiptAi
import app.coshell.core.domain.categoryguess.CategoryGuess
import app.coshell.core.domain.drafts.AccountLabels
import app.coshell.core.domain.drafts.QuickNoteDrafts
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * Runs the gold set through **the app's own code** (#742): quick notes go through the rules and [NoteHints] exactly as «Обработать» does,
 * an unknown payee through history, the brand dictionary and [PayeeCategoryAi], a receipt through [ReceiptAi] with its checks. So a score
 * here is the quality a person gets, not a model's raw skill. [model] `null` — rules only (the baseline the model has to beat).
 */
class AiEval(private val gold: EvalGold, private val model: LanguageModel?) {
    /** [misses] — one line per wrong case saying which field was wrong; [asked] — requests that reached the model. */
    class Score(val name: String, val ok: Int, val of: Int, val misses: List<String>, val asked: Int, val note: String = "") {
        /** «заметки (новый человек) 55/61» — what a recording keeps and the replay test compares. */
        val short: String get() = "$name $ok/$of"
        val line: String get() = short + (if (note.isNotEmpty()) " · $note" else "")
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

    private fun counting(): CountingModel? = model?.let(::CountingModel)

    /** All five measures in a fixed order (the order of a recording's `scores`). */
    suspend fun all(): List<Score> = listOf(notes(false), notes(true), payees(false), payees(true), receipts())

    /** Every note line, as one note processed at once; [withHistory] — the family has records (examples and rules learn from them). */
    suspend fun notes(withHistory: Boolean): Score {
        val counted = counting()
        val hints = counted?.let(::NoteHints)
        val history = if (withHistory) history() else emptyList()
        val ctx = QuickNoteDrafts.Context(accounts, categories, history, fallback = ars, known = setOf(ars, usd), now = NOW)
        val lines = gold.notes.map { it.line }
        val ask = hints?.let { QuickNoteDrafts.needsModel(lines, ctx, it.broad) }.orEmpty()
        val asked = ask.map { lines[it] }
        val examples = if (withHistory && asked.isNotEmpty()) HistoryExamples.pick(history, categories, asked) else emptyList()
        val read: List<NoteHint?> = hints?.takeIf { ask.isNotEmpty() }?.read(asked, gold.categories, accounts.map(AccountLabels::of), examples, CategoryHints.line(categories)).orEmpty()
        val byLine = ask.zip(read).mapNotNull { (i, h) -> h?.let { i to it } }.toMap()
        var ok = 0
        val misses = mutableListOf<String>()
        gold.notes.forEachIndexed { i, n ->
            val draft = QuickNoteDrafts.build(listOf(n.line), ctx, { "d$i" }, byLine[i]?.let { mapOf(0 to it) }.orEmpty()).firstOrNull()
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
        val name = if (withHistory) "заметки (с историей)" else "заметки (новый человек)"
        return Score(name, ok, gold.notes.size, misses, counted?.calls ?: 0, "в модель: ${ask.size} строк")
    }

    /**
     * A payee the person types for the first time: history (without this payee) and the brand dictionary first, then the model — the
     * order of the entry sheet. [withHistory] `false` — a new person: only the dictionary and the model without examples.
     */
    suspend fun payees(withHistory: Boolean, every: Int = 1): Score {
        val counted = counting()
        val ai = counted?.let(::PayeeCategoryAi)
        var ok = 0
        var byRules = 0
        val misses = mutableListOf<String>()
        val cases = gold.payees.filterIndexed { i, _ -> i % every == 0 }
        for (p in cases) {
            val history = if (withHistory) history(except = p.payee) else emptyList()
            val rules = CategoryGuess.suggest(history, TransactionType.EXPENSE, categories, payee = p.payee)
            val got = if (rules != null) {
                byRules++
                categories.first { it.id == rules.categoryId }.name
            } else {
                val examples = if (withHistory) HistoryExamples.pick(history, categories, listOf(p.payee)) else emptyList()
                ai?.suggest(p.payee, gold.categories, examples, CategoryHints.line(categories))
            }
            if (got in p.categories) ok++ else misses += "«${p.payee}»: ${got ?: "—"} ≠ ${p.category}" + if (rules != null) " (словарь/история)" else ""
        }
        val name = if (withHistory) "категория (с историей)" else "категория (новый человек)"
        return Score(name, ok, cases.size, misses, counted?.calls ?: 0, "по словарю и истории: $byRules")
    }

    /** Receipts no format knows: [ReceiptAi] with the app's checks (the total must be printed, the day within a year). */
    suspend fun receipts(): Score {
        val counted = counting()
        val ai = counted?.let(::ReceiptAi)
        var ok = 0
        val misses = mutableListOf<String>()
        val known = setOf(ars, usd, CurrencyCode("RUB"), CurrencyCode("AMD"))
        for (r in gold.receipts) {
            val got = ai?.read(r.text, ars, known, TODAY)
            val wrong = buildList {
                if (got == null) { add("не прочитан"); return@buildList }
                if (got.total.currency.value != r.currency) add("валюта ${got.total.currency.value} ≠ ${r.currency}")
                if (got.total.amountMinor != r.totalMinor) add("итог ${got.total.amountMinor} ≠ ${r.totalMinor}")
                if (got.date.toString() != r.date) add("дата ${got.date} ≠ ${r.date}")
                if (r.merchants.none { got.merchant.lowercase().contains(it.substringBefore(' ').lowercase()) }) add("магазин «${got.merchant}» ≠ «${r.merchant}»")
            }
            if (wrong.isEmpty()) ok++ else misses += "${r.id}: ${wrong.joinToString("; ")}"
        }
        return Score("чеки", ok, gold.receipts.size, misses, counted?.calls ?: 0)
    }

    /** Counts requests (to show how often the model is asked at all). */
    private class CountingModel(private val inner: LanguageModel) : LanguageModel {
        var calls = 0
        override val available: Boolean get() = inner.available
        override val cheap: Boolean get() = inner.cheap
        override suspend fun complete(system: String, prompt: String): String? { calls++; return inner.complete(system, prompt) }
    }

    companion object {
        /** «Today» of the gold set: notes say «вчера» against it, receipts must not be later. */
        val TODAY = LocalDate(2026, 11, 21)
        const val NOW = 1_795_273_200_000L // 2026-11-21T15:00:00Z
        const val DAY_MS = 86_400_000L
    }
}
