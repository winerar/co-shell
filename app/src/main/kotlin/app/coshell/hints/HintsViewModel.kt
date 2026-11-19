package app.coshell.hints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.data.family.FamilyService
import app.coshell.core.data.family.FamilyState
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.domain.hints.FirstStepsData
import app.coshell.core.domain.hints.FirstStepsState
import app.coshell.core.domain.hints.HintId
import app.coshell.core.domain.hints.StepsMoment
import app.coshell.core.domain.hints.StepsMoments
import app.coshell.R
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.domain.hints.LimitWatch
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import app.coshell.core.domain.hints.HintStore
import app.coshell.core.domain.hints.MomentFacts
import app.coshell.core.domain.hints.TimeMoments
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** «Первые шаги» on «Обзор» and the hints switch in «Ещё → Приложение» (#513, `docs/agents/hints.md`). */
@HiltViewModel
class HintsViewModel @Inject constructor(
    private val store: HintStore,
    accounts: AccountRepository,
    private val transactions: TransactionRepository,
    private val budgetSource: BudgetSource,
    private val moneyFormat: MoneyFormat,
    private val timeZone: TimeZone,
    budgets: BudgetRepository,
    family: FamilyService,
    private val center: HintCenter,
) : ViewModel() {

    private val _enabled = MutableStateFlow(true)
    private val hidden = MutableStateFlow(false)

    /** «Подсказки»: shown / off. */
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    init {
        viewModelScope.launch {
            _enabled.value = store.enabled()
            hidden.value = store.stepsHidden()
        }
    }

    private val data: Flow<FirstStepsData> = combine(
        accounts.observeAccounts().map { it.isNotEmpty() }.distinctUntilChanged(),
        transactions.observeTransactions(TransactionFilter(limit = 1)).map { it.isNotEmpty() }.distinctUntilChanged(),
        budgets.observeBudgets().map { it.isNotEmpty() }.distinctUntilChanged(),
        family.state.map { s -> ((s as? FamilyState.Joined)?.family?.members?.size ?: 0) > 1 }.distinctUntilChanged(),
    ) { account, entry, limit, partner -> FirstStepsData(account, entry, limit, partner) }.offMain()

    /** The steps the row shows; null while loading or when hints are off. Ticks come from the data. */
    val steps: StateFlow<FirstStepsState?> = combine(data, hidden, _enabled) { facts, isHidden, on ->
        if (on) FirstStepsState.of(facts, isHidden) else null
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _finale = MutableStateFlow(false)

    /** True once, right after the last step is done: «Всё готово» takes the place of the row (board `Hint513Finale`). */
    val finale: StateFlow<Boolean> = _finale.asStateFlow()

    init {
        // The warm moments come from a change of the data seen in this session, never from the state at start.
        viewModelScope.launch {
            var before: FirstStepsData? = null
            data.collect { now ->
                val moment = before?.let { StepsMoments.between(it, now) }
                before = now
                when (moment) {
                    StepsMoment.FIRST_ENTRY -> center.offer(mapOf(HintId.FIRST_ENTRY to R.string.hint_first_entry))
                    StepsMoment.FIRST_LIMIT -> center.offer(mapOf(HintId.FIRST_LIMIT to R.string.hint_first_limit))
                    StepsMoment.FINALE -> if (store.enabled() && !hidden.value && FINALE_KEY !in store.shown()) {
                        store.markShown(FINALE_KEY)
                        _finale.value = true
                    }
                    null -> Unit
                }
            }
        }
    }

    init {
        // Time-based moments are checked once per launch, a little after the start (never in the first seconds).
        viewModelScope.launch {
            delay(MOMENTS_DELAY_MS)
            val today = System.currentTimeMillis() / MILLIS_PER_DAY
            val recent = transactions.observeTransactions(TransactionFilter(limit = TimeMoments.BACKUP_ENTRIES)).first()
            val last = recent.maxOfOrNull { it.date.toEpochMilliseconds() / MILLIS_PER_DAY }
            val facts = MomentFacts(
                today = today,
                firstSeenDay = store.firstSeenDay(today),
                firstEntryDay = store.firstEntryDay(if (recent.isEmpty()) null else today),
                lastEntryDay = last,
                entryCount = recent.size,
                dayOfMonth = Clock.System.todayIn(timeZone).day,
            )
            val texts = TIME_MOMENT_TEXTS.filterKeys { it in TimeMoments.due(facts) }
            if (texts.isNotEmpty()) center.offer(texts)
            offerLimit80()
        }
        viewModelScope.launch { center.restoreDot() }
    }

    /** `limit_80`: the nearest limit that is 80–99 % spent this month, named with what is left (`LimitWatch`). */
    private suspend fun offerLimit80() {
        val snapshot = budgetSource.observe().first()
        val month = budgetPeriodOf(Clock.System.todayIn(timeZone), snapshot.startDay).month
        val near = LimitWatch.nearest(snapshot.calculator().month(month, Clock.System.todayIn(timeZone))) ?: return
        val name = snapshot.categories.firstOrNull { it.id == near.budget.categoryId }?.name ?: return
        val left = moneyFormat.copy(trimZeroFraction = true).format(near.left)
        center.offer(mapOf(HintId.LIMIT_80 to R.string.hint_limit_80), mapOf(HintId.LIMIT_80 to listOf(name, left)))
    }

    fun dismissFinale() {
        _finale.value = false
    }

    fun hideSteps() {
        hidden.value = true
        viewModelScope.launch { store.setStepsHidden(true) }
    }

    fun setEnabled(value: Boolean) {
        _enabled.value = value
        viewModelScope.launch { store.setEnabled(value) }
    }

    /** «Показать подсказки заново»: nothing is shown yet, the steps are back, hints are on. */
    fun reset() {
        _enabled.value = true
        hidden.value = false
        _finale.value = false
        viewModelScope.launch {
            store.reset()
            center.resetVisit()
            center.say(R.string.hints_reset_done)
        }
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
        const val MOMENTS_DELAY_MS = 6_000L
        val TIME_MOMENT_TEXTS = mapOf(
            HintId.MONTH_END to R.string.hint_month_end,
            HintId.FIRST_WEEK to R.string.hint_first_week,
            HintId.WELCOME_BACK to R.string.hint_welcome_back,
            HintId.BACKUP to R.string.hint_backup,
            HintId.THEME to R.string.hint_theme,
        )

        /** «Already shown» key of the finale; it is not a [HintId], `reset()` clears it with the rest. */
        const val FINALE_KEY = "steps_finale"
    }
}
