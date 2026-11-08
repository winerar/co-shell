package app.coshell.home

import androidx.lifecycle.ViewModel
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.sum
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stage 0 placeholder: shows demo accounts to prove that model, theme and DI are wired.
 * Replaced by a repository-backed implementation in stage 1.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val moneyFormat: MoneyFormat,
) : ViewModel() {

    private val _state = MutableStateFlow(buildState(demoAccounts))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private fun buildState(accounts: List<Pair<String, Money>>): HomeUiState {
        // No exchange rates yet (stage 3), so totals are shown per currency.
        val totals = accounts.map { it.second }
            .groupBy { it.currency }
            .map { (currency, items) -> moneyFormat.format(items.sum(currency)) }
        return HomeUiState(
            totals = totals,
            accounts = accounts.map { (name, money) ->
                HomeUiState.AccountRow(name, moneyFormat.format(money), money.isNegative)
            },
        )
    }

    private companion object {
        val RUB = CurrencyCode("RUB")
        val EUR = CurrencyCode("EUR")

        val demoAccounts = listOf(
            "Наличные" to Money.ofMajor(4_500, currency = RUB),
            "Карта" to Money.ofMajor(120_800, 50, RUB),
            "Кредитка" to Money.ofMajor(-15_000, currency = RUB),
            "Евро-счёт" to Money.ofMajor(1_250, currency = EUR),
        )
    }
}
