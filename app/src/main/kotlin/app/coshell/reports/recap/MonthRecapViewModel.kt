package app.coshell.reports.recap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.budgets.BudgetSource
import app.coshell.core.model.MoneyFormat
import app.coshell.navigation.MonthRecapDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn

/** «Итоги месяца» (#554) of the month in the route ([MonthRecapDestination]). Pure parts — `MonthRecapBoard.kt`. */
@HiltViewModel
class MonthRecapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    source: BudgetSource,
    moneyFormat: MoneyFormat,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {

    private val month = YearMonth(checkNotNull<Int>(savedStateHandle["year"]), checkNotNull<Int>(savedStateHandle["month"]))
    private val today = clock.todayIn(timeZone)
    private val format = moneyFormat.copy(trimZeroFraction = true)

    /** Where the review is counted; tests run it in place. Read once, on first [state]. */
    internal var work: CoroutineContext = Dispatchers.Default

    val state: StateFlow<MonthRecapUiState> by lazy {
        source.observe()
            .map { buildMonthRecap(it, month, today, format) }
            .flowOn(work)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), MonthRecapUiState(isLoading = true, month = month))
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
