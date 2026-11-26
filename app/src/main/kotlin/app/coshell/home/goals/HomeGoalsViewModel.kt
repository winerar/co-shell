package app.coshell.home.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.model.MoneyFormat
import app.coshell.goals.GoalRowUi
import app.coshell.goals.GoalWhen
import app.coshell.goals.GoalsSource
import app.coshell.goals.goalRowsOf
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The «Цели» card on «Обзоре» (#768, board `GoalHome`): the two goals in progress with the nearest deadline; none — no card. */
@HiltViewModel
class HomeGoalsViewModel @Inject constructor(source: GoalsSource, moneyFormat: MoneyFormat) : ViewModel() {
    private val format = moneyFormat.copy(trimZeroFraction = true)

    val rows: StateFlow<List<GoalRowUi>> = source.observe().map { snapshot ->
        goalRowsOf(snapshot, format).filter { it.whenKind != GoalWhen.REACHED }.take(SHOWN)
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private companion object {
        const val SHOWN = 2
    }
}
