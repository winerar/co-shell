package app.coshell.transactions.list

import app.coshell.core.domain.search.AmountQuery
import app.coshell.core.domain.search.PeriodRange
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/**
 * Labels of the filter chips (board `Fb141A`: «Еда · 3 ✕», «Октябрь ✕», «Аня ✕»). Pure, so the rules are tested;
 * words that need resources come in as parameters. null — the filter is not set (the chip shows its name).
 */
internal object FilterLabels {

    /**
     * One category — its name; exactly one whole folder — the folder; more — the first folder (or category)
     * and how many categories are chosen. «Без категории» counts as one more.
     */
    fun categories(filter: ListFilter, choices: FilterChoices, uncategorized: String): String? {
        val ids = filter.categoryIds
        if (ids.isEmpty()) return if (filter.uncategorized) uncategorized else null
        val byId = choices.categoriesById
        val wholeFolder = (choices.expenseCategories + choices.incomeCategories).firstOrNull { it.isFolder && it.ids == ids }
        // Leaves: a chosen folder counts as itself only when none of its children are listed.
        val leaves = ids.filter { id -> byId[id]?.isFolder != true }
        val count = leaves.size + (if (filter.uncategorized) 1 else 0)
        val first = ids.firstNotNullOfOrNull { id -> choices.folderOf[id] ?: byId[id] }?.name ?: return null
        return when {
            wholeFolder != null && !filter.uncategorized -> wholeFolder.name
            count <= 1 && !filter.uncategorized -> leaves.firstOrNull()?.let { byId[it]?.name } ?: first
            else -> "$first · $count"
        }
    }

    /** One account — its name; more — the first and the count («Galicia · 2»). */
    fun accounts(filter: ListFilter, choices: FilterChoices): String? {
        if (filter.accountIds.isEmpty()) return null
        val first = choices.accounts.firstOrNull { it.id in filter.accountIds }?.name ?: return null
        return if (filter.accountIds.size == 1) first else "$first · ${filter.accountIds.size}"
    }

    /** One tag — «# Испанский»; more — the first by name and the count («# Аня · 3»). Chosen tags since deleted are skipped. */
    fun tags(filter: ListFilter, choices: FilterChoices): String? {
        if (filter.tagIds.isEmpty()) return null
        val chosen = choices.tags.filter { it.id in filter.tagIds }
        val first = chosen.firstOrNull()?.name ?: return null
        return if (chosen.size == 1) "# $first" else "# $first · ${chosen.size}"
    }

    fun who(filter: ListFilter, choices: FilterChoices, me: String): String? = when (val w = filter.who) {
        null -> null
        ListFilter.Who.Me -> me
        is ListFilter.Who.Member -> choices.members.firstOrNull { it.id == w.id }?.name
    }

    /**
     * «Октябрь» (with the year when it is not this year), «Сен – ноя 2026» for three months, «2025» for a year,
     * «1 – 7 нояб.» for own days.
     */
    fun period(range: PeriodRange, today: LocalDate, locale: Locale): String {
        fun fmt(day: LocalDate, pattern: String) = day.toJavaLocalDate().format(DateTimeFormatter.ofPattern(pattern, locale))
        val sameYear = range.start.year == today.year && range.lastDay.year == today.year
        return when (range.step) {
            PeriodRange.Step.MONTH -> fmt(range.start, if (sameYear) "LLLL" else "LLLL yyyy").replaceFirstChar { it.titlecase(locale) }
            PeriodRange.Step.YEAR -> range.start.year.toString()
            PeriodRange.Step.THREE_MONTHS -> {
                val from = fmt(range.start, "LLL").trimEnd('.').replaceFirstChar { it.titlecase(locale) }
                val to = fmt(range.lastDay, if (sameYear) "LLL" else "LLL yyyy").trimEnd('.')
                "$from – $to"
            }
            PeriodRange.Step.DAYS -> {
                val yearPattern = if (sameYear) "" else " yyyy"
                if (range.start == range.lastDay) fmt(range.start, "d MMM$yearPattern")
                else "${fmt(range.start, "d MMM")} – ${fmt(range.lastDay, "d MMM$yearPattern")}"
            }
        }
    }

    /** "3 000", "41,20": an amount bound as typed, grouped by thousands. */
    fun hundredths(value: Long): String {
        val whole = (value / 100).toString().reversed().chunked(3).joinToString(" ").reversed()
        val cents = value % 100
        return if (cents == 0L) whole else "$whole,${cents.toString().padStart(2, '0')}"
    }

    /** Bounds of [query] for the hint under the search field, in the order its string takes them. */
    fun amountBounds(query: AmountQuery): List<String> =
        listOfNotNull(query.minHundredths, query.maxHundredths).distinct().map(::hundredths)
}
