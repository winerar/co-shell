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
) {
    enum class Period { ALL, THIS_MONTH, LAST_MONTH }

    val isActive: Boolean get() = query.isNotBlank() || kind != null || accountId != null || period != Period.ALL

    /** Search and type are checked in memory, so they need the whole (period/account-limited) list, not a page. */
    internal val needsAll: Boolean get() = query.isNotBlank() || kind != null
}

/** Account and period go to Room; the page [limit] only when nothing is filtered in memory. */
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
        limit = if (needsAll) null else limit,
    )
}

/**
 * Type and search: the query matches the note, category, account names or the amount, ignoring case and
 * spaces ("32000" finds "32 000,00 ARS").
 */
internal fun ListFilter.matches(
    t: Transaction,
    accountsById: Map<String, Account>,
    categoriesById: Map<String, Category>,
    moneyFormat: MoneyFormat,
): Boolean {
    val typeOk = when (kind) {
        null -> true
        Kind.EXPENSE -> t.type == TransactionType.EXPENSE
        Kind.INCOME -> t.type == TransactionType.INCOME
        Kind.TRANSFER -> t.type == TransactionType.TRANSFER
    }
    if (!typeOk) return false
    val needle = query.squash()
    if (needle.isEmpty()) return true
    val haystack = listOfNotNull(
        t.note,
        t.categoryId?.let { categoriesById[it]?.name },
        accountsById[t.accountId]?.name,
        t.toAccountId?.let { accountsById[it]?.name },
        moneyFormat.format(t.amount),
        t.originalAmount?.let(moneyFormat::format),
    )
    return haystack.any { needle in it.squash() }
}

private fun String.squash(): String = lowercase().filterNot { it.isWhitespace() }
