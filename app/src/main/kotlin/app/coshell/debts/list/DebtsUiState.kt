package app.coshell.debts.list

import androidx.compose.runtime.Immutable

/** One debt line (#552): everything is already formatted, the screen only draws it. */
@Immutable
data class DebtRowUi(
    val id: String,
    val person: String,
    /** «120 USD» — what is left. */
    val left: String,
    /** «из 300 USD» — empty when nothing was returned yet. */
    val ofTotal: String,
    val returnedFraction: Float,
    val due: DebtDue,
    val dueDate: String,
    /** Days to (or after, as a positive number) the due day; meaningful for [DebtDue.SOON] and [DebtDue.LATE]. */
    val days: Int,
    val note: String,
    val closed: Boolean,
)

/** How the due date looks on the line: never red (`hints.md` — «лимит — ориентир»). */
enum class DebtDue { NONE, SET, SOON, TODAY, LATE }

@Immutable
data class DebtsUiState(
    val isLoading: Boolean = true,
    /** 0 — «Мне должны», 1 — «Я должен». */
    val tab: Int = 0,
    /** «Мне должны: 200 USD · 45 000 RUB» — the totals of the active tab, one per currency. */
    val totals: List<String> = emptyList(),
    val active: List<DebtRowUi> = emptyList(),
    val closed: List<DebtRowUi> = emptyList(),
    val closedShown: Boolean = false,
    /** Both directions are empty: the whole screen is the empty state. */
    val nothingAtAll: Boolean = true,
)
