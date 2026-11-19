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
import app.coshell.core.domain.hints.HintStore
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
    transactions: TransactionRepository,
    budgets: BudgetRepository,
    family: FamilyService,
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

    /** The steps the row shows; null while loading or when hints are off. Ticks come from the data. */
    val steps: StateFlow<FirstStepsState?> = combine(
        accounts.observeAccounts().map { it.isNotEmpty() }.distinctUntilChanged(),
        transactions.observeTransactions(TransactionFilter(limit = 1)).map { it.isNotEmpty() }.distinctUntilChanged(),
        budgets.observeBudgets().map { it.isNotEmpty() }.distinctUntilChanged(),
        family.state.map { s -> ((s as? FamilyState.Joined)?.family?.members?.size ?: 0) > 1 }.distinctUntilChanged(),
        hidden,
        _enabled,
    ) { values ->
        val on = values[5] as Boolean
        if (!on) null else FirstStepsState.of(FirstStepsData(values[0] as Boolean, values[1] as Boolean, values[2] as Boolean, values[3] as Boolean), values[4] as Boolean)
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
        viewModelScope.launch { store.reset() }
    }
}
