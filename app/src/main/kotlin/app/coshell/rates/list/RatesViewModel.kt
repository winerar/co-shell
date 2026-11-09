package app.coshell.rates.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.rates.DownloadInfo
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.currentRates
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.RateOrigin
import app.coshell.rates.RatesRefresher
import app.coshell.rates.RefreshStatus
import app.coshell.ui.format.formatRate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/**
 * "More → Rates": rates in force today for every currency the family uses (`1 USD = X`),
 * manual rates with add / edit / delete, and "Update now" (a one-off run of the daily worker).
 */
@HiltViewModel
class RatesViewModel @Inject constructor(
    private val rates: ExchangeRateRepository,
    private val refresher: RatesRefresher,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    /** Screen-only state: dialogs and whether "Update now" was pressed on this screen. */
    private data class Local(
        val editor: ManualRateForm? = null,
        val pendingDelete: ManualRateItem? = null,
        val refreshRequested: Boolean = false,
    )

    private val local = MutableStateFlow(Local())
    private val used = MutableStateFlow<Set<CurrencyCode>?>(null)

    init {
        viewModelScope.launch { used.value = rates.usedCurrencies() }
    }

    val state: StateFlow<RatesUiState> = combine(
        rates.observeRates(),
        rates.observeLatestDownload(),
        used.filterNotNull(),
        refresher.observe(),
        local,
    ) { all, latest, used, refresh, local -> buildState(all, latest, used, refresh, local) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RatesUiState())

    private fun buildState(
        all: List<ExchangeRate>,
        latest: DownloadInfo?,
        used: Set<CurrencyCode>,
        refresh: RefreshStatus,
        local: Local,
    ): RatesUiState {
        val manual = all.filter { it.origin == RateOrigin.MANUAL }.sortedByDescending { it.date }
        val currencies = used + manual.flatMap { listOf(it.from, it.to) }
        val current = RateBook(all).currentRates(currencies, BASE, today()).map { row ->
            CurrentRateItem(
                currency = row.currency.value,
                rateText = row.found?.let { formatRate(it.rate) },
                date = row.found?.date,
                isManual = row.found?.origin == RateOrigin.MANUAL,
                viaPivot = row.found?.viaPivot == true,
            )
        }
        return RatesUiState(
            isLoading = false,
            base = BASE.value,
            current = current,
            lastDownload = latest,
            manual = manual.map { ManualRateItem(it, formatRate(it.rate)) },
            // A finished run from an earlier visit is old news; a running one is always shown.
            refresh = if (local.refreshRequested || refresh == RefreshStatus.Running) refresh else RefreshStatus.Idle,
            editor = local.editor,
            pendingDelete = local.pendingDelete,
        )
    }

    fun refreshNow() {
        local.update { it.copy(refreshRequested = true) }
        refresher.refreshNow()
    }

    /** "Add a rate": `1 USD = ? <first currency>` from today. */
    fun addManual() = openEditor(BASE.value, state.value.current.firstOrNull()?.currency.orEmpty(), "")

    /** Tap on a current rate: set it by hand, starting from the rate in force. */
    fun setManual(item: CurrentRateItem) = openEditor(BASE.value, item.currency, item.rateText.orEmpty())

    fun editManual(item: ManualRateItem) {
        val rate = item.rate
        openEditor(rate.from.value, rate.to.value, rate.rate.toPlainString().replace('.', ','), rate.date, rate)
    }

    private fun openEditor(from: String, to: String, rate: String, date: LocalDate? = null, original: ExchangeRate? = null) {
        val today = today()
        local.update { it.copy(editor = ManualRateForm(from, to, rate, date ?: today, today, original)) }
    }

    fun onFromChange(value: String) = updateEditor { it.copy(from = value.uppercase(), fromError = null, toError = null) }
    fun onToChange(value: String) = updateEditor { it.copy(to = value.uppercase(), toError = null) }
    fun onRateChange(value: String) = updateEditor { it.copy(rate = value, rateError = false) }
    fun onDateSelect(date: LocalDate) = updateEditor { it.copy(date = date) }
    fun onDatePicked(millisUtc: Long) =
        onDateSelect(Instant.fromEpochMilliseconds(millisUtc).toLocalDateTime(TimeZone.UTC).date)

    fun dismissEditor() = local.update { it.copy(editor = null) }

    fun saveManual() {
        val form = local.value.editor ?: return
        val (checked, rate) = form.validate()
        if (rate == null) {
            local.update { it.copy(editor = checked) }
            return
        }
        local.update { it.copy(editor = null) }
        viewModelScope.launch {
            // Moving an edited rate to another day or pair: the old record goes away.
            form.original?.takeUnless { it.sameSlotAs(rate) }?.let { rates.deleteManualRate(it.date, it.from, it.to) }
            rates.saveManualRate(rate)
        }
    }

    fun askDelete(item: ManualRateItem) = local.update { it.copy(pendingDelete = item) }
    fun cancelDelete() = local.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val rate = local.value.pendingDelete?.rate ?: return
        local.update { it.copy(pendingDelete = null) }
        viewModelScope.launch { rates.deleteManualRate(rate.date, rate.from, rate.to) }
    }

    private fun updateEditor(change: (ManualRateForm) -> ManualRateForm) =
        local.update { s -> s.copy(editor = s.editor?.let(change)) }

    private fun today(): LocalDate = clock.todayIn(timeZone)

    private companion object {
        /** Until the main currency setting exists (stage 3, step «д»), rates are shown against USD. */
        val BASE = CurrencyCode("USD")
    }
}
