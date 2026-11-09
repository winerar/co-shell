package app.coshell.currencies.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.settings.ReportCurrencies
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.CurrencyCode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Edits the report currency list: add, remove, make main (list rules — [ReportCurrencies]). */
@HiltViewModel
class ReportCurrenciesViewModel @Inject constructor(
    private val settings: ReportCurrencySettings,
    private val rates: ExchangeRateRepository,
) : ViewModel() {

    private val adding = MutableStateFlow<AddCurrencyForm?>(null)

    val state: StateFlow<ReportCurrenciesUiState> = combine(settings.currencies, adding) { list, form ->
        ReportCurrenciesUiState(isLoading = false, currencies = list.map { it.value }, adding = form)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ReportCurrenciesUiState())

    fun openAdd() {
        viewModelScope.launch {
            val current = state.value.currencies.toSet()
            val suggestions = rates.usedCurrencies().map { it.value }.filter { it !in current }.sorted()
            adding.value = AddCurrencyForm(suggestions = suggestions)
        }
    }

    fun onCodeChange(value: String) = adding.update { it?.copy(code = value.trim().uppercase(), error = false) }

    fun dismissAdd() {
        adding.value = null
    }

    fun confirmAdd() {
        val form = adding.value ?: return
        val code = CurrencyCode.parseOrNull(form.code)
        if (code == null) {
            adding.value = form.copy(error = true)
            return
        }
        adding.value = null
        edit { ReportCurrencies.add(it, code) }
    }

    fun makeMain(code: String) = edit { ReportCurrencies.makeMain(it, CurrencyCode(code)) }

    fun remove(code: String) = edit { ReportCurrencies.remove(it, CurrencyCode(code)) }

    private fun edit(change: (List<CurrencyCode>) -> List<CurrencyCode>) {
        val current = state.value.currencies.map(::CurrencyCode)
        viewModelScope.launch { settings.setCurrencies(change(current)) }
    }
}
