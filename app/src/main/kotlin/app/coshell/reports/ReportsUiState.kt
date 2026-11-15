package app.coshell.reports

import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.ReportPeriodKind
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * «Отчёты» (boards `RepA`, `RepPeriod`): the period and the report currency, «На что ушли деньги» and «По месяцам».
 * Amounts are formatted numbers without the code (the code is shown once, next to the total and on the currency pill).
 */
data class ReportsUiState(
    val isLoading: Boolean = false,
    val period: ReportPeriod? = null,
    /** The family's month start day: months, quarters and years follow it. */
    val startDay: Int = 1,
    val today: LocalDate? = null,
    /** The report currency («USD»); [currencies] — the totals currencies to pick from, the main one first. */
    val currency: String = "",
    val currencies: List<String> = emptyList(),
    /** «Сравнивать с прошлым периодом»: the grey marks on the bars and the line under the total. */
    val compare: Boolean = true,
    val spending: Spending? = null,
    val months: Months? = null,
    /** «Доходы и расходы» (#189); `null` — nothing came in or went out in the shown months. */
    val income: Income? = null,
    /** «Кто сколько потратил» (#189); empty — one-person family or nothing spent. */
    val members: List<MemberRow> = emptyList(),
) {
    /** Nothing was spent in the period (the cards say so instead of empty charts). */
    val isEmpty: Boolean get() = !isLoading && spending == null

    data class Spending(
        /** «760» — net spending, refunds taken off. */
        val total: String,
        val comparison: Comparison?,
        val bars: List<Bar>,
        val other: Other?,
        /** Every category of the period, flat — «Все категории» (`RepAll`). */
        val categories: List<CategoryRow>,
        /** Currencies left out for lack of a rate: «без учёта: AMD». */
        val missing: List<String> = emptyList(),
    )

    /** The line under the total. */
    sealed interface Comparison {
        /** «за 11 дней · в прошлом месяце к 11-му было 820»; [previousDay] — the day number «к 11-му». */
        data class SoFar(val days: Int, val kind: ReportPeriodKind, val previousDay: Int, val previous: String, val faster: Boolean) : Comparison

        /** A finished period: «в прошлом месяце — 820». */
        data class Whole(val kind: ReportPeriodKind, val previous: String, val more: Boolean) : Comparison
    }

    /** One bar: a top-level category or a category with subcategories (they add up into it). */
    data class Bar(
        val categoryId: String,
        /** `null` — a category deleted on another phone while its operations stayed. */
        val name: String?,
        val icon: String?,
        val color: Long?,
        val value: String,
        val percent: Int,
        /** Of the chart's one scale (the largest bar or ghost). */
        val fraction: Float,
        /** Where the previous period was; `null` — not compared. */
        val ghost: Float?,
        /** The previous period's amount for TalkBack. */
        val ghostValue: String?,
        /** «↑ 45%» — spending noticeably more or less than usual (#238); `null` — as usual or not a month. */
        val mark: Mark? = null,
    )

    /**
     * «Выше / ниже обычного» (#237, #238): [up] — more than usual; [percent] — by how much (no sign), `null` — nothing
     * was usually spent («новое»); [expected] — the usual amount by now; [byDay] — «к 11-му», `null` — a whole month.
     */
    data class Mark(val up: Boolean, val percent: Int?, val expected: String, val byDay: Int?)

    /** «Другое · Транспорт и ещё 3 · 70 · 9%». */
    data class Other(val firstName: String?, val more: Int, val value: String, val percent: Int)

    data class CategoryRow(
        val categoryId: String,
        /** `null` — a category deleted on another phone while its operations stayed. */
        val name: String?,
        val icon: String?,
        val color: Long?,
        val value: String,
        val percent: Int,
        val fraction: Float,
        val ghost: Float?,
        val ghostValue: String?,
        /** Spending in the period before (minor units) and now: «По росту» orders by the difference. */
        val growthMinor: Long,
        val amountMinor: Long,
        /** The parent category, `null` — top level («Подкатегориями» groups by it). */
        val parentId: String?,
        val mark: Mark? = null,
    )

    /** «По месяцам»: spending of each month, the last month of the period highlighted. */
    data class Months(val columns: List<MonthColumn>, val selected: Int)

    data class MonthColumn(val month: YearMonth, val value: String, val fraction: Float)

    /**
     * «Доходы и расходы» (board `Rp189IeA`): income and spending of each month of «По месяцам» on one scale; under the
     * chart — the [selected] month: what came in, went out and was left ([saved] signed: «+270», «-110»).
     */
    data class Income(
        val columns: List<IncomeColumn>,
        val selected: Int,
        val earned: String,
        val spent: String,
        val saved: String,
        val overspent: Boolean,
        /** The selected month's savings rate, `null` — nothing came in. */
        val rate: Int?,
        /** The selected month is still running: its numbers are «пока». */
        val running: Boolean,
        /** The average rate of the finished months and how many there were; `null` — none. */
        val averageRate: Int?,
        val averageMonths: Int,
    )

    data class IncomeColumn(
        val month: YearMonth,
        val earned: String,
        val spent: String,
        val earnedFraction: Float,
        val spentFraction: Float,
    )

    /**
     * A row of «Кто сколько потратил» (board `Rp189WhoA`): [name] `null` — «Я» (everything the others did not add);
     * [top] — the folders this member spent most on («Дом 262»).
     */
    data class MemberRow(
        val memberId: String?,
        val name: String?,
        val value: String,
        val percent: Int,
        val fraction: Float,
        val top: List<TopFolder>,
    )

    /** [name] `null` — a category deleted on another phone. */
    data class TopFolder(val name: String?, val value: String)
}
