package app.coshell.transactions.list

import app.coshell.core.model.Account
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.tagIds
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import app.coshell.transactions.list.TransactionsUiState.Row
import app.coshell.transactions.list.TransactionsUiState.RowTag
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * Groups [transactions] (newest first) by local day in [timeZone] and formats every row.
 * Pure: names come from the given maps; archived records are fine, deleted ones fall back to null/"?".
 * Tags come from [tagsById] (live tags only): a reference to a deleted or unknown tag shows no pill.
 */
internal fun buildTransactionDays(
    transactions: List<Transaction>,
    accountsById: Map<String, Account>,
    categoriesById: Map<String, Category>,
    moneyFormat: MoneyFormat,
    timeZone: TimeZone,
    today: LocalDate,
    authors: Map<String, String> = emptyMap(),
    tagsById: Map<String, Tag> = emptyMap(),
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
                rows = items.map { t ->
                    t.toRow(accountsById, categoriesById, moneyFormat)
                        .copy(author = t.authorMemberId?.let(authors::get))
                        .withTags(t, tagsById)
                        .copy(money = t.amount)
                },
            ).withTotal(items, moneyFormat)
        }
}

private fun Row.withTags(t: Transaction, tagsById: Map<String, Tag>): Row {
    if (t.labels.isEmpty() || tagsById.isEmpty()) return this
    val live = t.tagIds.mapNotNull(tagsById::get)
    if (live.isEmpty()) return this
    return copy(
        tags = live.take(MAX_ROW_TAGS).map { RowTag(it.name, it.color ?: CategoryPalette.forKey(it.id)) },
        moreTags = (live.size - MAX_ROW_TAGS).coerceAtLeast(0),
    )
}

/** Net per currency in the order the currencies first appear; zero nets are left out. */
private fun Day.withTotal(items: List<Transaction>, moneyFormat: MoneyFormat): Day {
    val nets = linkedMapOf<CurrencyCode, Long>()
    items.forEach { t ->
        val sign = when (t.type) {
            TransactionType.EXPENSE -> -1
            TransactionType.INCOME -> 1
            TransactionType.TRANSFER -> return@forEach
        }
        nets[t.amount.currency] = (nets[t.amount.currency] ?: 0L) + sign * t.amount.amountMinor
    }
    val shown = nets.filterValues { it != 0L }
    if (shown.isEmpty()) return this
    val text = shown.entries.joinToString(" · ") { (currency, minor) ->
        val money = Money(minor, currency)
        if (minor > 0) "+" + moneyFormat.format(money) else moneyFormat.format(money)
    }
    val tone = when {
        shown.values.all { it < 0 } -> Kind.EXPENSE
        shown.values.all { it > 0 } -> Kind.INCOME
        else -> null
    }
    return copy(total = text, totalTone = tone)
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
            refund = category?.type == CategoryType.EXPENSE,
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
