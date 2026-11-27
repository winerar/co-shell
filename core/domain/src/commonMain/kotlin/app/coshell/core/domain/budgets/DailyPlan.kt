package app.coshell.core.domain.budgets

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.recurring.due
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/** What «Сегодня можно» is counted from (#769, poll «Б»): the month's limits, or — without limits — its income. */
enum class DailyBase { LIMITS, INCOME }

/** «Как посчитано»: every line of the sum, in [currency]; [allowance] — the result. */
data class DailyPlan(
    val base: DailyBase,
    val period: BudgetPeriod,
    val baseAmount: Money,
    val spent: Money,
    val upcoming: Money,
    val goals: Money,
    val allowance: Allowance,
    /** Currencies left out for lack of a rate. */
    val missing: Set<CurrencyCode> = emptySet(),
)

/**
 * Builds [DailyPlan] for [today] (#769). With limits in the budget month — their sum against spending in the limited categories;
 * without — the month's income against all spending (no income yet — no plan, `null`). Then minus the regular expenses still due
 * before the month ends ([templates] with «Повтор» and an amount, missed ones of this month too) and [goalsPerMonth] (poll «А»:
 * savings first). Days left — to the end of the budget month (`month_start_day`), today included.
 */
object DailyPlanner {
    fun plan(
        budgets: List<Budget>,
        categories: List<Category>,
        days: List<ReportDay>,
        book: RateBook,
        templates: List<TransactionTemplate>,
        goalsPerMonth: Money?,
        currency: CurrencyCode,
        startDay: Int,
        today: LocalDate,
    ): DailyPlan? {
        val period = budgetPeriodOf(today, startDay)
        val ledger = BudgetLedger(days, book)
        val todayOnly = BudgetPeriod(period.month, today, today.plus(DatePeriod(days = 1)))
        val calculator = BudgetCalculator(budgets, categories, ledger, book, startDay)
        val limited = calculator.limitedCategoryIds(period.month)
        val missing = linkedSetOf<CurrencyCode>()
        val base: DailyBase
        val baseAmount: Money
        val spent: Money
        val spentToday: Money
        if (limited.isNotEmpty()) {
            val summary = calculator.summary(period.month, today, currency)
            missing += summary.missing
            base = DailyBase.LIMITS
            baseAmount = summary.planned
            spent = summary.spent
            spentToday = ledger.spent(limited, todayOnly, currency).sum
        } else {
            val expenseIds = categories.filter { it.type == CategoryType.EXPENSE }.mapTo(mutableSetOf()) { it.id }
            val income = income(days, categories, period, book, currency, missing)
            if (!income.isPositive) return null
            base = DailyBase.INCOME
            baseAmount = income
            spent = ledger.spent(expenseIds, period, currency).also { missing += it.missing }.sum
            spentToday = ledger.spent(expenseIds, todayOnly, currency).sum
        }
        val upcoming = upcoming(templates, period, today, book, currency, missing)
        val goals = goalsPerMonth?.takeIf { it.currency == currency } ?: Money.zero(currency)
        val allowance = DailyAllowance.of(baseAmount, spent, spentToday, upcoming, goals, today.daysUntil(period.endExclusive).coerceAtLeast(1))
        return DailyPlan(base, period, baseAmount, spent, upcoming, goals, allowance, missing)
    }

    /** Income of the period in income categories (not «Корректировка баланса»), by the rate of each day. */
    private fun income(days: List<ReportDay>, categories: List<Category>, period: BudgetPeriod, book: RateBook, currency: CurrencyCode, missing: MutableSet<CurrencyCode>): Money {
        val incomeIds = categories.filter { it.type == CategoryType.INCOME }.mapTo(mutableSetOf()) { it.id }
        var sum = Money.zero(currency)
        for (d in days) {
            if (d.type != TransactionType.INCOME || d.day !in period || d.categoryId !in incomeIds || SystemCategories.isAdjustment(d.categoryId)) continue
            val rate = book.find(d.amount.currency, currency, d.day)?.rate
            if (rate == null) missing += d.amount.currency else sum += rate.convert(d.amount, currency)
        }
        return sum
    }

    /** Regular expenses still to pay this budget month: every unhandled payment day up to its last day, by today's rate. */
    private fun upcoming(templates: List<TransactionTemplate>, period: BudgetPeriod, today: LocalDate, book: RateBook, currency: CurrencyCode, missing: MutableSet<CurrencyCode>): Money {
        var sum = Money.zero(currency)
        for (t in templates) {
            val schedule = t.schedule ?: continue
            val amount = t.amount ?: continue
            if (t.type != TransactionType.EXPENSE) continue
            val count = schedule.due(period.lastDay).count { it >= period.start }
            if (count == 0) continue
            val rate = book.find(amount.currency, currency, today)?.rate
            if (rate == null) { missing += amount.currency; continue }
            sum += rate.convert(Money(amount.amountMinor * count, amount.currency), currency)
        }
        return sum
    }
}
