package app.coshell.goals.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.model.MoneyFormat
import app.coshell.goals.GoalWhen
import app.coshell.goals.GoalWhatIfSource
import app.coshell.goals.GoalsSource
import app.coshell.goals.goalWhatIfs
import app.coshell.goals.goalRowsOf
import app.coshell.goals.goalTotalsOf
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** «Цели» (#768): rows and totals from [GoalsSource], «Что если» (#792) from [GoalWhatIfSource]; reached goals are folded until asked. */
@HiltViewModel
class GoalsViewModel @Inject constructor(source: GoalsSource, whatIf: GoalWhatIfSource, moneyFormat: MoneyFormat) : ViewModel() {
    private val format = moneyFormat.copy(trimZeroFraction = true)
    private val reachedShown = MutableStateFlow(false)

    val state: StateFlow<GoalsUiState> = combine(source.observe(), whatIf.observe(), reachedShown) { snapshot, data, shown ->
        val rows = goalRowsOf(snapshot, format)
        GoalsUiState(
            isLoading = false,
            totals = goalTotalsOf(snapshot, format),
            active = rows.filter { it.whenKind != GoalWhen.REACHED },
            reached = rows.filter { it.whenKind == GoalWhen.REACHED },
            reachedShown = shown,
            whatIf = goalWhatIfs(snapshot.lines, data, snapshot.accounts, snapshot.today, format),
        )
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    fun toggleReached() = reachedShown.update { !it }
}
