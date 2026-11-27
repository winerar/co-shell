package app.coshell.home.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.budgets.DailyPlanner
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.model.MoneyFormat
import app.coshell.goals.GoalsSource
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * «Сегодня можно» (#769, poll «А·Б·А»): [DailyPlanner] over the budget snapshot, regular payments and the goals' month; `null` — no card
 * (no limits and no income this month). Apart from `HomeViewModel`, like the budget tile: it reads the day sums of the whole history.
 */
@HiltViewModel
class HomeDailyViewModel @Inject constructor(
    source: BudgetSource,
    templates: TemplateRepository,
    goals: GoalsSource,
    moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val format = moneyFormat.copy(trimZeroFraction = true)

    val state: StateFlow<DailyUi?> = combine(source.observe(), templates.observeTemplates(), goals.observe()) { snapshot, list, goalSnap ->
        val perMonth = goalSnap.perMonth.sum.takeIf { it.currency == snapshot.mainCurrency }
        DailyPlanner.plan(
            snapshot.budgets, snapshot.categories, snapshot.days, snapshot.book, list, perMonth,
            snapshot.mainCurrency, snapshot.startDay, clock.todayIn(timeZone),
        )?.let { dailyUiOf(it, format) }
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
