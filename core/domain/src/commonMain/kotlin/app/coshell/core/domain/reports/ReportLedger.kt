package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate

/** Sums by category in one currency; amounts in [missing] currencies have no rate and are left out. */
data class CategorySums(val byCategory: Map<String, Money>, val missing: Set<CurrencyCode>) {
    fun total(currency: CurrencyCode): Money = byCategory.values.fold(Money.zero(currency)) { acc, it -> acc + it }
}

/**
 * Report money in [currency], each day at the rate of that day (ARCHITECTURE §6) — the same rules as budgets
 * (`BudgetLedger`): an expense is spending; an income in an expense category is a refund and reduces the
 * spending; an income in an income category is income. «Корректировка баланса» is neither.
 * A category missing from [categories] (deleted) keeps its operations: an expense spends, an income earns.
 */
class ReportLedger(
    days: List<ReportDay>,
    categories: List<Category>,
    private val book: RateBook,
    val currency: CurrencyCode,
) {
    private class Entry(val day: LocalDate, val categoryId: String, val amount: Money)

    private val spending = mutableListOf<Entry>()
    private val income = mutableListOf<Entry>()

    init {
        val types = categories.associate { it.id to it.type }
        for (d in days) {
            if (SystemCategories.isAdjustment(d.categoryId)) continue
            when (d.type) {
                TransactionType.EXPENSE -> spending += Entry(d.day, d.categoryId, d.amount)
                TransactionType.INCOME ->
                    if (types[d.categoryId] == CategoryType.EXPENSE) spending += Entry(d.day, d.categoryId, -d.amount)
                    else income += Entry(d.day, d.categoryId, d.amount)
                TransactionType.TRANSFER -> Unit
            }
        }
    }

    /** Net spending (refunds taken off) of every category with operations in [from] ..< [toExclusive]. */
    fun spent(from: LocalDate, toExclusive: LocalDate): CategorySums = sum(spending, from, toExclusive)

    fun earned(from: LocalDate, toExclusive: LocalDate): CategorySums = sum(income, from, toExclusive)

    fun spent(period: BudgetPeriod): CategorySums = spent(period.start, period.endExclusive)

    fun earned(period: BudgetPeriod): CategorySums = earned(period.start, period.endExclusive)

    private fun sum(entries: List<Entry>, from: LocalDate, toExclusive: LocalDate): CategorySums {
        val sums = linkedMapOf<String, Money>()
        val missing = linkedSetOf<CurrencyCode>()
        for (e in entries) {
            if (e.day < from || e.day >= toExclusive) continue
            val rate = book.find(e.amount.currency, currency, e.day)?.rate
            if (rate == null) { missing += e.amount.currency; continue }
            val converted = rate.convert(e.amount, currency)
            sums[e.categoryId] = (sums[e.categoryId] ?: Money.zero(currency)) + converted
        }
        return CategorySums(sums, missing)
    }
}
