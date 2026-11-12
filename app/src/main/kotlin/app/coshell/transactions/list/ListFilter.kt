package app.coshell.transactions.list

import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.rates.monthBounds
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.transactions.list.TransactionsUiState.Kind
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

/** Search and filter chips of «Операции». */
data class ListFilter(
    val query: String = "",
    val kind: Kind? = null,
    val accountId: String? = null,
    val period: Period = Period.ALL,
    /** Only expenses and incomes without a category (opened from «Обзор» → «Внимание»). */
    val uncategorized: Boolean = false,
) {
    enum class Period { ALL, THIS_MONTH, LAST_MONTH }

    val isActive: Boolean
        get() = query.isNotBlank() || kind != null || accountId != null || period != Period.ALL || uncategorized

    /** Only the search is checked in memory, so it needs the whole (Room-filtered) list (#261). */
    internal val needsAll: Boolean get() = query.isNotBlank()

    /** What goes to Room: a new query runs only when this changes, not on every typed letter (#261). */
    internal val roomKey: ListFilter get() = ListFilter(query = if (needsAll) "*" else "", kind = kind, accountId = accountId, period = period, uncategorized = uncategorized)

    companion object {
        /** What «Внимание» counts: this month's expenses and incomes without a category. */
        val UncategorizedThisMonth = ListFilter(period = Period.THIS_MONTH, uncategorized = true)
    }
}

/** Account, period, type and «без категории» go to Room; the page [limit] only when nothing is searched in memory. */
internal fun ListFilter.toRepositoryFilter(limit: Int, today: LocalDate, timeZone: TimeZone): TransactionFilter {
    val bounds = when (period) {
        ListFilter.Period.ALL -> null
        ListFilter.Period.THIS_MONTH -> monthBounds(today, timeZone)
        ListFilter.Period.LAST_MONTH -> monthBounds(today.minus(DatePeriod(months = 1)), timeZone)
    }
    return TransactionFilter(
        accountId = accountId,
        from = bounds?.first,
        to = bounds?.second,
        type = when (kind) {
            null -> null
            Kind.EXPENSE -> TransactionType.EXPENSE
            Kind.INCOME -> TransactionType.INCOME
            Kind.TRANSFER -> TransactionType.TRANSFER
        },
        uncategorized = uncategorized,
        limit = if (needsAll) null else limit,
    )
}

/**
 * Type and search: the query matches the note, category, account names or the amount, ignoring case and
 * spaces ("32000" finds "32 000,00 ARS"). Type and «без категории» are also applied by Room; checked here too.
 */
internal fun ListFilter.matches(
    t: Transaction,
    accountsById: Map<String, Account>,
    categoriesById: Map<String, Category>,
    moneyFormat: MoneyFormat,
): Boolean = matches(t, SearchIndex(accountsById, categoriesById, moneyFormat))

internal fun ListFilter.matches(t: Transaction, index: SearchIndex): Boolean {
    val typeOk = when (kind) {
        null -> true
        Kind.EXPENSE -> t.type == TransactionType.EXPENSE
        Kind.INCOME -> t.type == TransactionType.INCOME
        Kind.TRANSFER -> t.type == TransactionType.TRANSFER
    }
    if (!typeOk) return false
    if (uncategorized && (t.type == TransactionType.TRANSFER || t.categoryId != null)) return false
    val needle = query.squash()
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
) {
    private val texts = HashMap<String, String>()

    fun text(t: Transaction): String = texts.getOrPut(t.id) {
        listOfNotNull(
            t.note,
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

private fun String.squash(): String = lowercase().filterNot { it.isWhitespace() }
