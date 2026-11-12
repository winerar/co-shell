package app.coshell.budgets.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.model.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * «Бюджеты»: the limits of the chosen month against what was spent (the calculator — [BudgetSource]),
 * the month total in the main report currency, the filter chips and folded folders. Opens on the
 * current budget month (by the family's start day); arrows go to any month before or after.
 */
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    source: BudgetSource,
    moneyFormat: MoneyFormat,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {

    private val today = clock.todayIn(timeZone)
    private val format = moneyFormat.copy(trimZeroFraction = true)

    /** `null` — the current month, whatever the start day turns out to be. */
    private val chosenMonth = MutableStateFlow<YearMonth?>(null)
    private val filter = MutableStateFlow(BudgetFilter.ALL)
    private val collapsed = MutableStateFlow<Set<String>>(emptySet())

    private val board = combine(source.observe(), chosenMonth) { snapshot, chosen ->
        val month = chosen ?: budgetPeriodOf(today, snapshot.startDay).month
        val calculator = snapshot.calculator()
        val period = snapshot.period(month)
        val envelopes = buildEnvelopes(calculator.month(month, today), snapshot.categories, format)
        BudgetsUiState(
            month = month,
            periodDays = (period.start to period.lastDay).takeIf { period.start.day != 1 },
            summary = if (envelopes.isEmpty()) null else buildSummary(calculator.summary(month, today, snapshot.mainCurrency), today, format),
            envelopes = envelopes,
            counts = envelopeCounts(envelopes),
        )
    }.flowOn(Dispatchers.Default)

    val state: StateFlow<BudgetsUiState> = combine(board, filter, collapsed) { base, chosenFilter, folded ->
        // A filter that emptied (the last «горит» limit calmed down) would hide everything with no chip to leave it.
        val shown = chosenFilter.takeIf { it == BudgetFilter.ALL || (base.counts[it] ?: 0) > 0 } ?: BudgetFilter.ALL
        base.copy(filter = shown, collapsed = folded)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), BudgetsUiState(isLoading = true))

    fun onPreviousMonth() = shiftMonth(-1)

    fun onNextMonth() = shiftMonth(1)

    fun onFilterChange(value: BudgetFilter) { filter.value = value }

    fun onToggleFolder(categoryId: String) = collapsed.update { if (categoryId in it) it - categoryId else it + categoryId }

    private fun shiftMonth(by: Int) {
        val shown = state.value.month ?: return
        chosenMonth.value = if (by > 0) shown.plus(by, DateTimeUnit.MONTH) else shown.minus(-by, DateTimeUnit.MONTH)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
