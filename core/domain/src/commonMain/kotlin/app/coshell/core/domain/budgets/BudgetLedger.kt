package app.coshell.core.domain.budgets

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.isBalanceAdjustment
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/** Spending of some categories over a period. Amounts in [missing] currencies have no rate and are not in [sum]. */
data class Spent(val sum: Money, val missing: Set<CurrencyCode> = emptySet())

/** A category with a limit covers itself and, when it is a folder, all its subcategories (archived too). */
fun budgetCategoryIds(categoryId: String, categories: List<Category>): Set<String> =
    categories.filter { it.parentId == categoryId }.mapTo(mutableSetOf(categoryId)) { it.id }

/**
 * What was spent by category, for budgets. Expenses count with the amount debited from the account;
 * an income put into the same category is a refund and reduces the spending. Transfers and
 * «Корректировка баланса» are not spending. Each operation is converted at the rate of its own day
 * (ARCHITECTURE §6), so a closed month does not change when rates move.
 */
class BudgetLedger(transactions: List<Transaction>, private val book: RateBook, timeZone: TimeZone) {

    private class Entry(val day: LocalDate, val amount: Money, val refund: Boolean)

    private val byCategory: Map<String, List<Entry>> = buildMap<String, MutableList<Entry>> {
        for (t in transactions) {
            val categoryId = t.categoryId ?: continue
            if (!t.countsForBudget) continue
            val entry = Entry(t.date.toLocalDateTime(timeZone).date, t.amount, refund = t.type == TransactionType.INCOME)
            getOrPut(categoryId) { mutableListOf() } += entry
        }
    }

    /** Day of the earliest operation with a category, `null` when there are none. */
    val firstDay: LocalDate? = byCategory.values.flatten().minOfOrNull { it.day }

    fun spent(categoryIds: Set<String>, period: BudgetPeriod, currency: CurrencyCode): Spent {
        var sum = Money.zero(currency)
        val missing = linkedSetOf<CurrencyCode>()
        for (id in categoryIds) for (entry in byCategory[id].orEmpty()) {
            if (entry.day !in period) continue
            val rate = book.find(entry.amount.currency, currency, entry.day)?.rate
            if (rate == null) { missing += entry.amount.currency; continue }
            val converted = rate.convert(entry.amount, currency)
            sum = if (entry.refund) sum - converted else sum + converted
        }
        return Spent(sum, missing)
    }

    /**
     * Average monthly spending over the [months] periods before [month] — the hint for a new limit.
     * Periods before the first recorded operation are not counted, so a family that has kept records
     * for one month gets that month, not a third of it. `null` — no history or nothing spent.
     */
    fun averageSpent(
        categoryIds: Set<String>,
        month: YearMonth,
        currency: CurrencyCode,
        startDay: Int = DEFAULT_MONTH_START_DAY,
        months: Int = 3,
    ): Money? {
        val first = firstDay ?: return null
        val periods = (1..months)
            .map { budgetPeriod(month.minus(it, DateTimeUnit.MONTH), startDay) }
            .filter { it.endExclusive > first }
        if (periods.isEmpty()) return null
        val total = periods.fold(Money.zero(currency)) { acc, period -> acc + spent(categoryIds, period, currency).sum }
        return total.scale(1, periods.size.toLong()).takeIf { it.isPositive }
    }
}

/** Transfers and «Корректировка баланса» are not spending; an income in a category is a refund (see [BudgetLedger]). */
private val Transaction.countsForBudget: Boolean
    get() = categoryId != null && type != TransactionType.TRANSFER && !isBalanceAdjustment

/**
 * The operations behind a limit's numbers (envelope screen, #183): those of [categoryIds] in [period] that
 * [BudgetLedger] counts — expenses and refunds, newest first.
 */
fun budgetOperations(transactions: List<Transaction>, categoryIds: Set<String>, period: BudgetPeriod, timeZone: TimeZone): List<Transaction> =
    transactions
        .filter { it.countsForBudget && it.categoryId in categoryIds && it.date.toLocalDateTime(timeZone).date in period }
        .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })
