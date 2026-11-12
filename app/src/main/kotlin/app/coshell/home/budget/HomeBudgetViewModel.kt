package app.coshell.home.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.budgets.BudgetSource
import app.coshell.core.model.MoneyFormat
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
import kotlinx.datetime.todayIn

/**
 * The budget tile and «Горит в бюджете» on «Обзор» ([buildHomeBudget]). Kept apart from `HomeViewModel`: the budget
 * calculator reads the whole history of day sums, and a family without limits pays nothing for it on the main screen.
 */
@HiltViewModel
class HomeBudgetViewModel @Inject constructor(
    private val source: BudgetSource,
    moneyFormat: MoneyFormat,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {

    private val today = clock.todayIn(timeZone)
    private val format = moneyFormat.copy(trimZeroFraction = true)

    /** Where the budget is computed; tests run it in place. Read once, on first [state]. */
    internal var work: CoroutineContext = Dispatchers.Default

    /** `null` until the first count: «Обзор» waits for it, so the budget tile does not replace another one in sight. */
    val state: StateFlow<Loaded?> by lazy {
        source.observe()
            .map { Loaded(buildHomeBudget(it, today, format)) }
            .flowOn(work)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)
    }

    /** [budget] — `null` when the month has no limits. */
    data class Loaded(val budget: HomeBudget?)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
