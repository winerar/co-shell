package app.coshell.budgets.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.budgets.SaveBudgetUseCase
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.domain.budgets.suggestBudgetLimits
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.model.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
 * The gear changes the start day (#213) — a family setting, synced to the other phone.
 */
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val source: BudgetSource,
    private val monthStart: MonthStartSettings,
    private val saveBudget: SaveBudgetUseCase,
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

    /** Rows of the first-visit offer the person ticked off (#185); everything is ticked at first. */
    private val unticked = MutableStateFlow<Set<String>>(emptySet())

    /** Where the board is computed (it indexes all operations); tests run it in place. Read once, on first [state]. */
    internal var work: CoroutineContext = Dispatchers.Default

    private fun board(): Flow<BudgetsUiState> = combine(source.observe(), chosenMonth) { snapshot, chosen ->
        val month = chosen ?: budgetPeriodOf(today, snapshot.startDay).month
        val calculator = snapshot.calculator()
        val period = snapshot.period(month)
        val envelopes = buildEnvelopes(calculator.month(month, today), snapshot.categories, format)
        BudgetsUiState(
            month = month,
            startDay = snapshot.startDay,
            periodDays = (period.start to period.lastDay).takeIf { period.start.day != 1 },
            summary = if (envelopes.isEmpty()) null else buildSummary(calculator.summary(month, today, snapshot.mainCurrency), today, format),
            envelopes = envelopes,
            counts = envelopeCounts(envelopes),
            offer = if (envelopes.isEmpty()) {
                buildOffer(suggestBudgetLimits(snapshot.ledger, snapshot.categories, month, snapshot.mainCurrency, snapshot.startDay), snapshot.categories, format)
            } else {
                null
            },
        )
    }.flowOn(work)

    val state: StateFlow<BudgetsUiState> by lazy { stateFlow() }

    private fun stateFlow(): StateFlow<BudgetsUiState> = combine(board(), filter, collapsed, unticked) { base, chosenFilter, folded, off ->
        // A filter that emptied (the last «горит» limit calmed down) would hide everything with no chip to leave it.
        val shown = chosenFilter.takeIf { it == BudgetFilter.ALL || (base.counts[it] ?: 0) > 0 } ?: BudgetFilter.ALL
        val offer = base.offer?.let { o -> o.copy(rows = o.rows.map { it.copy(checked = it.categoryId !in off) }) }
        base.copy(filter = shown, collapsed = folded, offer = offer)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), BudgetsUiState(isLoading = true))

    fun onPreviousMonth() = shiftMonth(-1)

    fun onNextMonth() = shiftMonth(1)

    fun onFilterChange(value: BudgetFilter) { filter.value = value }

    /** [day] — 1..31; the shown month stays, its days follow the new start. */
    fun onStartDayChange(day: Int) {
        viewModelScope.launch { monthStart.setMonthStartDay(day) }
    }

    /** A row of the first-visit offer ticked on or off. */
    fun onToggleSuggestion(categoryId: String) = unticked.update { if (categoryId in it) it - categoryId else it + categoryId }

    /** «Создать N лимитов»: the ticked rows become limits from the shown month on; the amounts are edited later. */
    fun onCreateSuggested() {
        val month = state.value.month ?: return
        val rows = state.value.offer?.rows?.filter { it.checked } ?: return
        viewModelScope.launch { rows.forEach { saveBudget(it.categoryId, month, it.limit) } }
    }

    fun onToggleFolder(categoryId: String) = collapsed.update { if (categoryId in it) it - categoryId else it + categoryId }

    private fun shiftMonth(by: Int) {
        val shown = state.value.month ?: return
        chosenMonth.value = if (by > 0) shown.plus(by, DateTimeUnit.MONTH) else shown.minus(-by, DateTimeUnit.MONTH)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
