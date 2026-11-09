package app.coshell.transactions.list

import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * Groups [transactions] (newest first) by local day in [timeZone] and formats every row.
 * Pure: names come from the given maps; archived records are fine, deleted ones fall back to null/"?".
 */
internal fun buildTransactionDays(
    transactions: List<Transaction>,
    accountsById: Map<String, Account>,
    categoriesById: Map<String, Category>,
    moneyFormat: MoneyFormat,
    timeZone: TimeZone,
    today: LocalDate,
    authors: Map<String, String> = emptyMap(),
): List<Day> {
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    return transactions
        .sortedByDescending { it.date }
        .groupBy { it.date.toLocalDateTime(timeZone).date }
        .map { (date, items) ->
            Day(
                date = date,
                relative = when (date) {
                    today -> RelativeDay.TODAY
                    yesterday -> RelativeDay.YESTERDAY
                    else -> RelativeDay.OTHER
                },
                rows = items.map { t -> t.toRow(accountsById, categoriesById, moneyFormat).copy(author = t.authorMemberId?.let(authors::get)) },
            )
        }
}

private fun Transaction.toRow(
    accountsById: Map<String, Account>,
    categoriesById: Map<String, Category>,
    moneyFormat: MoneyFormat,
): Row {
    val accountName = accountsById[accountId]?.name ?: UNKNOWN
    val category = categoryId?.let { categoriesById[it] }
    return when (type) {
        TransactionType.EXPENSE -> Row(
            id = id,
            kind = Kind.EXPENSE,
            title = category?.name,
            icon = category?.icon,
            color = category?.color,
            subtitle = accountName,
            note = note,
            amount = moneyFormat.format(-amount),
            secondaryAmount = originalAmount?.let(moneyFormat::format),
        )
        TransactionType.INCOME -> Row(
            id = id,
            kind = Kind.INCOME,
            title = category?.name,
            icon = category?.icon,
            color = category?.color,
            subtitle = accountName,
            note = note,
            amount = "+" + moneyFormat.format(amount),
            secondaryAmount = originalAmount?.let(moneyFormat::format),
        )
        TransactionType.TRANSFER -> {
            val target = toAccountId?.let { accountsById[it]?.name } ?: UNKNOWN
            val arriving = toAmount
            Row(
                id = id,
                kind = Kind.TRANSFER,
                title = null,
                subtitle = "$accountName → $target",
                note = note,
                amount = moneyFormat.format(amount),
                secondaryAmount = arriving?.takeIf { it.currency != amount.currency }
                    ?.let { "+" + moneyFormat.format(it) },
            )
        }
    }
}

/** Name of an account deleted on the other phone before this one synced. */
private const val UNKNOWN = "?"
