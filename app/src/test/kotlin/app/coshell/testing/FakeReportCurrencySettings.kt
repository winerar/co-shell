package app.coshell.testing

import app.coshell.core.domain.settings.ReportCurrencies
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.CurrencyCode
import kotlinx.coroutines.flow.MutableStateFlow

/** Report currencies in memory; starts with [ReportCurrencySettings.DEFAULT] (USD). */
class FakeReportCurrencySettings(
    initial: List<CurrencyCode> = ReportCurrencySettings.DEFAULT,
) : ReportCurrencySettings {
    override val currencies = MutableStateFlow(initial)
    override suspend fun setCurrencies(currencies: List<CurrencyCode>) {
        this.currencies.value = ReportCurrencies.normalize(currencies)
    }
}
