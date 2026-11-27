package app.coshell.goals

import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.domain.goals.WHAT_IF_MONTHS
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportRepository
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.model.Category
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** What «Что если» (#792) is counted from: the last finished months' day sums, balance moves, categories, regular payments, rates. */
class WhatIfData(
    val months: List<BudgetPeriod>,
    val days: List<ReportDay>,
    val balanceDays: List<BalanceDay>,
    val categories: List<Category>,
    /** Category ids of templates with «Повтор»: rent and the like are not spending one can cut. */
    val scheduled: Set<String>,
    val book: RateBook,
)

/** Reads [WhatIfData] for «Цели»: three finished family months by SQL day sums (#187), never the operations themselves. */
class GoalWhatIfSource @Inject constructor(
    private val reports: ReportRepository,
    private val categories: CategoryRepository,
    private val templates: TemplateRepository,
    private val rates: ExchangeRateRepository,
    private val monthStart: MonthStartSettings,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<WhatIfData> = monthStart.monthStartDay.flatMapLatest { startDay ->
        val now = budgetPeriodOf(clock.todayIn(timeZone), startDay)
        val months = (WHAT_IF_MONTHS downTo 1).map { back -> (1..back).fold(now) { p, _ -> p.previous(startDay) } }
        combine(
            reports.observeDays(months.first().start, now.start),
            reports.observeBalanceDays(),
            categories.observeCategories(includeArchived = true),
            templates.observeTemplates(),
            rates.observeRates(),
        ) { days, balances, categoryList, templateList, rateList ->
            WhatIfData(
                months = months,
                days = days,
                balanceDays = balances,
                categories = categoryList,
                scheduled = templateList.filter { it.schedule != null }.mapNotNull { it.categoryId }.toSet(),
                book = RateBook.of(rateList),
            )
        }
    }
}
