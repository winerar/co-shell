package app.coshell.budgets.list

import app.coshell.core.designsystem.components.BulletTone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** «Бюджеты» (mockups `BudA`, `BudMany`): the month, its total and the envelopes. Strings are ready, captions are typed. */
data class BudgetsUiState(
    val isLoading: Boolean = false,
    val month: YearMonth? = null,
    /** First and last day of the period when the month does not start on the 1st (payday); `null` — calendar month. */
    val periodDays: Pair<LocalDate, LocalDate>? = null,
    val summary: Summary? = null,
    /** All envelopes of the month, folders with their inner limits; [visible] — after the filter. */
    val envelopes: List<Envelope> = emptyList(),
    val filter: BudgetFilter = BudgetFilter.ALL,
    /** How many envelopes (inner ones too) each filter chip would show. */
    val counts: Map<BudgetFilter, Int> = emptyMap(),
    /** Folders the user folded; their inner limits are hidden. */
    val collapsed: Set<String> = emptySet(),
) {
    val isEmpty: Boolean get() = !isLoading && envelopes.isEmpty()
    val total: Int get() = counts[BudgetFilter.ALL] ?: 0

    /** Chips appear only on a long list (`BudMany`): with a handful of limits everything is on screen anyway. */
    val showFilter: Boolean get() = total >= FILTER_FROM

    val visible: List<Envelope> get() = envelopes.mapNotNull { it.filtered(filter) }

    /** The month in one currency (the main report currency). */
    data class Summary(
        /** Formatted with the code: «540 USD»; overspent — the overspending, see [over]. */
        val left: String,
        val over: Boolean,
        val spent: String,
        val planned: String,
        val fraction: Float,
        val pace: Float?,
        val tone: BulletTone,
        /** Days after today; `null` — not the current month. */
        val daysLeft: Int?,
        val forecast: Forecast?,
        /** Currencies left out for lack of a rate. */
        val missing: List<String> = emptyList(),
    )

    /** «К концу месяца при таком темпе ≈ [amount]»; [overBy] — how much above the plan, `null` — within it. */
    data class Forecast(val amount: String, val overBy: String?)

    data class Envelope(
        /** The category or folder the limit is on. */
        val categoryId: String,
        val name: String,
        val icon: String?,
        val color: Long?,
        /** Subcategories in a folder, `null` — a plain category. */
        val folderSize: Int?,
        /** Number only, no code: «310 000». */
        val spent: String,
        /** With the code: «400 000 ARS». */
        val limit: String,
        val fraction: Float,
        val pace: Float?,
        val tone: BulletTone,
        val caption: Caption,
        /** Carried over from earlier months, signed with the code («+5 000 ARS»); `null` — nothing carried. */
        val carried: String? = null,
        val hasNote: Boolean = false,
        /** Limits on categories of this folder (each also counts toward the folder's). */
        val children: List<Envelope> = emptyList(),
        val missing: List<String> = emptyList(),
    ) {
        fun matches(filter: BudgetFilter): Boolean = when (filter) {
            BudgetFilter.ALL -> true
            BudgetFilter.HOT -> tone == BulletTone.WARNING || tone == BulletTone.DANGER
            BudgetFilter.IN_PLAN -> tone == BulletTone.NORMAL
            BudgetFilter.IDLE -> tone == BulletTone.IDLE
        }

        /** A folder stays while it or one of its limits matches; then only the matching limits are under it. */
        fun filtered(filter: BudgetFilter): Envelope? {
            if (filter == BudgetFilter.ALL) return this
            val inner = children.filter { it.matches(filter) }
            return when {
                matches(filter) -> copy(children = inner)
                inner.isNotEmpty() -> copy(children = inner)
                else -> null
            }
        }
    }

    /** The line under a bullet. Amounts are formatted, with the code. */
    sealed interface Caption {
        /** «осталось 220 000 ARS · 11 600 в день». */
        data class Left(val left: String, val perDay: String?) : Caption
        /** «обгоняете темп · осталось 90 000 ARS на 19 дней». */
        data class Ahead(val left: String, val days: Int) : Caption
        /** «перерасход 15 000 ARS». */
        data class Over(val over: String) : Caption
        /** «не тратили». */
        data object Idle : Caption
    }

    companion object {
        const val FILTER_FROM = 8
    }
}

/** Chips of a long list (`BudMany`): «Все · Горит · В плане · Без трат». */
enum class BudgetFilter { ALL, HOT, IN_PLAN, IDLE }
