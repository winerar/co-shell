package app.coshell.reports.category

import app.coshell.budgets.list.number
import app.coshell.core.designsystem.charts.ChartScale
import app.coshell.core.domain.budgets.budgetCategoryIds
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.categoryDrill
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.isBalanceAdjustment
import app.coshell.reports.ReportsUiState.Bar
import app.coshell.transactions.list.buildTransactionDays
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/** What the drill is counted from; [ledger] must hold the days of [period] and of the period before it. */
internal class DrillInput(
    val categoryId: String,
    val ledger: ReportLedger,
    val categories: List<Category>,
    val period: ReportPeriod,
    val startDay: Int,
    val compare: Boolean,
    /** The period's operations of the category and its subcategories (any order). */
    val operations: List<Transaction>,
    val accounts: List<Account>,
    val authors: Map<String, String>,
    val timeZone: TimeZone,
    val today: LocalDate,
)

/** The category and its subcategories: what the drill shows operations of. */
internal fun drillCategoryIds(categoryId: String, categories: List<Category>): Set<String> = budgetCategoryIds(categoryId, categories)

/**
 * Pure: «Из чего сложилось». The head and the bars are the report's numbers (each day at its own rate, refunds taken off);
 * the grey mark of a bar is the same subcategory in the period before, on the one scale of the bars. The operations are
 * the expenses and refunds behind them, by day, each in its own currency.
 */
internal fun buildCategoryDrill(input: DrillInput, format: MoneyFormat, moneyFormat: MoneyFormat): ReportCategoryUiState {
    val currency = input.ledger.currency
    val byId = input.categories.associateBy { it.id }
    val sums = input.ledger.spent(input.period.start, input.period.endExclusive)
    val drill = categoryDrill(sums, input.categoryId, input.categories, currency)
    val previous = input.period.previous(input.startDay)
    val previousSums = if (input.compare) input.ledger.spent(previous.start, previous.endExclusive) else null

    val parts = drill?.parts.orEmpty()
    fun ghostOf(id: String): Money? = previousSums?.let { it.byCategory[id] ?: Money.zero(currency) }
    val scale = ChartScale.of(parts.flatMap { listOf(it.amount.amountMinor, ghostOf(it.categoryId)?.amountMinor) })
    val bars = parts.map { part ->
        val category = byId[part.categoryId]
        val ghost = ghostOf(part.categoryId)
        Bar(
            categoryId = part.categoryId,
            name = category?.name,
            icon = category?.icon,
            color = category?.color,
            value = format.number(part.amount),
            percent = part.percent,
            fraction = scale.of(part.amount.amountMinor),
            ghost = ghost?.let { scale.of(it.amountMinor) },
            ghostValue = ghost?.let { format.number(it) },
        )
    }

    val ids = drillCategoryIds(input.categoryId, input.categories)
    val shown = input.operations.filter { it.categoryId in ids && it.type != TransactionType.TRANSFER && !it.isBalanceAdjustment }
    val days = buildTransactionDays(
        shown,
        input.accounts.associateBy { it.id },
        byId,
        moneyFormat,
        input.timeZone,
        input.today,
        input.authors,
    )
    return ReportCategoryUiState(
        name = byId[input.categoryId]?.name,
        period = input.period,
        currency = currency.value,
        total = drill?.let { format.number(it.amount) },
        percent = drill?.percent ?: 0,
        parts = bars,
        days = days,
        missing = sums.missing.map { it.value }.sorted(),
    )
}
