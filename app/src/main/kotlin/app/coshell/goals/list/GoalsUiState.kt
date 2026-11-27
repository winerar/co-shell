package app.coshell.goals.list

import androidx.compose.runtime.Immutable
import app.coshell.goals.GoalRowUi
import app.coshell.goals.GoalTotalsUi

/** «Цели» (#768): totals on top, goals in progress by deadline, reached ones folded below. */
@Immutable
data class GoalsUiState(
    val isLoading: Boolean = true,
    val totals: GoalTotalsUi = GoalTotalsUi("", "", ""),
    val active: List<GoalRowUi> = emptyList(),
    val reached: List<GoalRowUi> = emptyList(),
    val reachedShown: Boolean = false,
) {
    val isEmpty: Boolean get() = active.isEmpty() && reached.isEmpty()
}
