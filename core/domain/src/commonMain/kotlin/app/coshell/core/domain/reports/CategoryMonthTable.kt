package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money

/**
 * A row of the table «категории × месяцы» (#239): [months] — one amount per column, [total] — their sum,
 * [average] — [total] over the number of columns. A folder has [children] (its subcategories, and its own operations
 * as the part with the folder's id); a plain category has none.
 */
data class TableRow(
    val categoryId: String,
    val months: List<Money>,
    val total: Money,
    val average: Money,
    val children: List<TableRow> = emptyList(),
)

/** The bottom line of a block: the sum of every row by month. */
data class TableTotals(val months: List<Money>, val total: Money, val average: Money)

/**
 * The table of a period (#239, research П2): [spending] — folders (open into categories) and top-level categories,
 * largest total first; [income] — the same for income, a block apart. [periods] are the columns, oldest first.
 * [missing] — currencies left out for lack of a rate.
 */
data class CategoryMonthTable(
    val periods: List<BudgetPeriod>,
    val spending: List<TableRow>,
    val spendingTotals: TableTotals,
    val income: List<TableRow>,
    val incomeTotals: TableTotals,
    val missing: Set<CurrencyCode> = emptySet(),
) {
    val isEmpty: Boolean get() = spending.isEmpty() && income.isEmpty()
}

/**
 * Net spending (refunds taken off) and income of each of [periods] by category — the rules of #187. The ledger must
 * hold the days of all [periods]: a year of day sums, not of operations. A row with nothing in any month is left out.
 */
fun categoryMonthTable(ledger: ReportLedger, categories: List<Category>, periods: List<BudgetPeriod>): CategoryMonthTable {
    require(periods.isNotEmpty()) { "periods must not be empty" }
    val spent = periods.map { ledger.spent(it) }
    val earned = periods.map { ledger.earned(it) }
    val currency = ledger.currency
    val (spending, spendingTotals) = block(spent, categories, currency)
    val (income, incomeTotals) = block(earned, categories, currency)
    return CategoryMonthTable(periods, spending, spendingTotals, income, incomeTotals, (spent + earned).flatMap { it.missing }.toSet())
}

private fun block(sums: List<CategorySums>, categories: List<Category>, currency: CurrencyCode): Pair<List<TableRow>, TableTotals> {
    val byId = categories.associateBy { it.id }
    fun folderOf(id: String): String = byId[id]?.parentId?.takeIf { it in byId } ?: id
    val columns = sums.size

    fun row(id: String, ids: Collection<String>, children: List<TableRow> = emptyList()): TableRow {
        val months = sums.map { month -> ids.fold(Money.zero(currency)) { acc, it -> acc + (month.byCategory[it] ?: Money.zero(currency)) } }
        val total = months.fold(Money.zero(currency)) { acc, it -> acc + it }
        return TableRow(id, months, total, total.scale(1, columns.toLong()), children)
    }

    fun TableRow.nonEmpty() = months.any { it.amountMinor != 0L }
    fun List<TableRow>.largestFirst() = sortedWith(compareByDescending<TableRow> { it.total.amountMinor }.thenBy { it.categoryId })

    val used = sums.flatMap { it.byCategory.keys }.toSet()
    val rows = used.groupBy { folderOf(it) }.map { (folderId, members) ->
        val isFolder = members.any { it != folderId } || categories.any { it.parentId == folderId }
        val children = if (isFolder) members.map { row(it, listOf(it)) }.filter { it.nonEmpty() }.largestFirst() else emptyList()
        row(folderId, members, children)
    }.filter { it.nonEmpty() }.largestFirst()

    val months = List(columns) { i -> rows.fold(Money.zero(currency)) { acc, r -> acc + r.months[i] } }
    val total = months.fold(Money.zero(currency)) { acc, it -> acc + it }
    return rows to TableTotals(months, total, total.scale(1, columns.toLong()))
}
