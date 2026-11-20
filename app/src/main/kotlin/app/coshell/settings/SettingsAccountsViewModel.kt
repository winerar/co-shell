package app.coshell.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.Account
import kotlinx.coroutines.flow.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The live accounts for the settings that pick one: «Ввод операции → Всегда этот счёт» and «Счета в общем балансе». The balance
 * flag is the account's own `excludedFromTotal`, so it is shared by the family and synced like any account edit.
 */
@HiltViewModel
class SettingsAccountsViewModel @Inject constructor(
    private val accounts: AccountRepository,
    reportCurrencies: ReportCurrencySettings,
) : ViewModel() {

    /** The main report currency («50 USD» in «Трата партнёра»). */
    val mainCurrency: StateFlow<String> = reportCurrencies.currencies.map { (it.firstOrNull() ?: ReportCurrencySettings.DEFAULT.first()).value }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ReportCurrencySettings.DEFAULT.first().value)

    /** All totals currencies, main first (codes): the «Деньги и даты» card of «Настройки» joins them. */
    val currencies: StateFlow<List<String>> = reportCurrencies.currencies.map { list -> list.map { it.value } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ReportCurrencySettings.DEFAULT.map { it.value })


    val list: StateFlow<List<Account>> =
        accounts.observeAccounts(includeArchived = false).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /** [inTotal] false — the account is left out of the total balance. */
    fun setInTotal(id: String, inTotal: Boolean) {
        viewModelScope.launch {
            val account = accounts.getAccount(id) ?: return@launch
            if (account.excludedFromTotal == !inTotal) return@launch
            accounts.saveAccount(account.copy(excludedFromTotal = !inTotal))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
