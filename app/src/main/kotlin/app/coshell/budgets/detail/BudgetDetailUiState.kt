package app.coshell.budgets.detail

import app.coshell.budgets.list.BudgetsUiState.Envelope
import app.coshell.transactions.list.TransactionsUiState
import kotlinx.datetime.YearMonth

/**
 * One envelope (mockup `BudDetail`, #183): what is left, the bullet with today's mark, the note, the categories
 * of a folder and the month's operations behind the numbers. Strings are ready.
 */
data class BudgetDetailUiState(
    val isLoading: Boolean = false,
    val month: YearMonth? = null,
    /** The category or folder name; «—» when it was deleted on the other phone. */
    val name: String = "",
    /** `null` once loaded — no limit in this month (removed, or set from a later month). */
    val head: Head? = null,
    val note: String? = null,
    /** Categories of a folder: with their own limit or without; empty for a plain category. */
    val categories: List<Inner> = emptyList(),
    val operations: List<TransactionsUiState.Row> = emptyList(),
) {
    val hasNoLimit: Boolean get() = !isLoading && head == null

    /** The top card: the row data of the limit plus what only the envelope screen says. */
    data class Head(
        val envelope: Envelope,
        /** Left, or the overspending when [over]; with the code: «90 000 ARS». */
        val left: String,
        val over: Boolean,
        /** Days after today; `null` — not the current month. */
        val daysLeft: Int?,
        /** «к сегодня должно быть ≈ 147 000»; `null` — not the current month. */
        val paceToday: String?,
    )

    sealed interface Inner {
        val categoryId: String

        /** A category with its own limit inside the folder: a row like on «Бюджеты». */
        data class Limited(val envelope: Envelope) : Inner {
            override val categoryId: String get() = envelope.categoryId
        }

        /** «без своего лимита»; [spent] — with the code, `null` — nothing spent this month. */
        data class Plain(
            override val categoryId: String,
            val name: String,
            val icon: String?,
            val color: Long?,
            val spent: String?,
        ) : Inner
    }
}
