package app.coshell.transactions.list

import app.coshell.core.domain.AuthorFilter
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.search.AmountQuery
import app.coshell.core.domain.search.PeriodRange
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.tagIds
import app.coshell.transactions.list.TransactionsUiState.Kind
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * Search and filter chips of «Операции» (#141, variant А): each chip has its own sheet. Empty sets mean
 * «все»; [uncategorized] adds operations without a category to the chosen categories.
 */
data class ListFilter(
    val query: String = "",
    val kind: Kind? = null,
    val accountIds: Set<String> = emptySet(),
    val categoryIds: Set<String> = emptySet(),
    /** Expenses and incomes without a category (alone — opened from «Обзор» → «Внимание»). */
    val uncategorized: Boolean = false,
    /** null — all time. */
    val period: PeriodRange? = null,
    /** «Кто внёс»; null — anyone. */
    val who: Who? = null,
    /** Operations with any of these tags (#294); checked in memory — tags live in `labels`, not in a column. */
    val tagIds: Set<String> = emptySet(),
) {
    sealed interface Who {
        data object Me : Who
        data class Member(val id: String) : Who
    }

    /** `=4500`, `>10000`, `3000-5000`: the query is an amount search, not text. Parsed once per filter. */
    val amount: AmountQuery? by lazy { AmountQuery.parse(query) }

    internal val needle: String by lazy { query.squash() }

    val isActive: Boolean
        get() = query.isNotBlank() || kind != null || accountIds.isNotEmpty() || categoryIds.isNotEmpty() ||
            uncategorized || period != null || who != null || tagIds.isNotEmpty()

    /** A set filter shows its total (4А), which needs every matching row, not a page (#141). */
    internal val needsAll: Boolean get() = isActive

    /** What goes to Room: a new query runs only when this changes, not on every typed letter (#261). */
    internal val roomKey: ListFilter
        get() = copy(query = if (query.isNotBlank()) "*" else "", tagIds = if (tagIds.isNotEmpty()) ANY_TAG else emptySet())

    companion object {
        /** What «Внимание» counts: this month's expenses and incomes without a category. */
        fun uncategorizedThisMonth(today: LocalDate) =
            ListFilter(period = PeriodRange.Preset.THIS_MONTH.range(today), uncategorized = true)

        /** Stands for «some tags» in [roomKey]: Room loads the same rows whichever tags are chosen. */
        private val ANY_TAG = setOf("*")
    }
}

/**
 * Everything but the text goes to Room; the page [limit] only when no filter is set. [others] — the other
 * family members, so «Я» means «not added by them».
 */
internal fun ListFilter.toRepositoryFilter(limit: Int, timeZone: TimeZone, others: Set<String> = emptySet()): TransactionFilter {
    val bounds = period?.bounds(timeZone)
    return TransactionFilter(
        accountIds = accountIds.takeIf { it.isNotEmpty() },
        categoryIds = categoryIds.takeIf { it.isNotEmpty() },
        from = bounds?.first,
        to = bounds?.second,
        type = kind?.toType(),
        uncategorized = uncategorized,
        author = when (val w = who) {
            null -> null
            ListFilter.Who.Me -> AuthorFilter.Me(others)
            is ListFilter.Who.Member -> AuthorFilter.Member(w.id)
        },
        limit = if (needsAll) null else limit,
    )
}

private fun Kind.toType() = when (this) {
    Kind.EXPENSE -> TransactionType.EXPENSE
    Kind.INCOME -> TransactionType.INCOME
    Kind.TRANSFER -> TransactionType.TRANSFER
}

/**
 * Search: an amount query ([ListFilter.amount]) compares amounts in any currency; other text matches the
 * note, payee, labels, category, account names or the formatted amount, ignoring case and spaces
 * ("32000" finds "32 000,00 ARS"); tag names count too. Type, accounts and categories are applied by Room and
 * checked here too; the period and the author — by Room only; tags — here only.
 */
internal fun ListFilter.matches(
    t: Transaction,
    accountsById: Map<String, Account>,
    categoriesById: Map<String, Category>,
    moneyFormat: MoneyFormat,
): Boolean = matches(t, SearchIndex(accountsById, categoriesById, moneyFormat))

internal fun ListFilter.matches(t: Transaction, index: SearchIndex): Boolean {
    if (kind != null && t.type != kind.toType()) return false
    if (accountIds.isNotEmpty() && t.accountId !in accountIds && t.toAccountId !in accountIds) return false
    if (categoryIds.isNotEmpty() || uncategorized) {
        val bare = t.type != TransactionType.TRANSFER && t.categoryId == null
        val inCategory = t.categoryId != null && t.categoryId in categoryIds
        if (!(inCategory || (uncategorized && bare))) return false
    }
    if (tagIds.isNotEmpty() && t.tagIds.none { it in tagIds }) return false
    amount?.let { return it.matches(t) }
    return needle.isEmpty() || needle in index.text(t)
}

/**
 * Searchable text of each operation, formatted once per loaded list (#261): each typed letter then only
 * scans ready strings instead of formatting thousands of amounts again. Used from one flow at a time.
 */
internal class SearchIndex(
    private val accountsById: Map<String, Account>,
    private val categoriesById: Map<String, Category>,
    private val moneyFormat: MoneyFormat,
    /** Live tags: an operation is found by a tag's name even when its label is an id. */
    private val tagsById: Map<String, Tag> = emptyMap(),
) {
    private val texts = HashMap<String, String>()

    fun text(t: Transaction): String = texts.getOrPut(t.id) {
        listOfNotNull(
            t.note,
            t.payee,
            t.labels.joinToString(FIELD_BREAK).takeIf { it.isNotEmpty() },
            t.tagIds.mapNotNull { tagsById[it]?.name }.joinToString(FIELD_BREAK).takeIf { it.isNotEmpty() },
            t.categoryId?.let { categoriesById[it]?.name },
            accountsById[t.accountId]?.name,
            t.toAccountId?.let { accountsById[it]?.name },
            moneyFormat.format(t.amount),
            t.originalAmount?.let(moneyFormat::format),
        ).joinToString(FIELD_BREAK) { it.squash() }
    }

    private companion object {
        /** Keeps a query from matching across two fields. */
        const val FIELD_BREAK = "\u0000"
    }
}

internal fun String.squash(): String = lowercase().filterNot { it.isWhitespace() }
