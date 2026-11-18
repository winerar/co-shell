package app.coshell.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.currentRates
import app.coshell.core.domain.settings.ConverterMode
import app.coshell.core.domain.settings.ConverterPick
import app.coshell.core.domain.settings.ConverterSettings
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.RateOrigin
import app.coshell.ui.format.formatRate
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
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
import kotlinx.datetime.todayIn

/**
 * The converter sheet (#319): an amount typed on the own keypad, shown as a pair («Отдаю / Получаю») or in every currency of the
 * list. The typed number lives here only; the mode and the pair are remembered on the phone ([ConverterSettings]).
 */
@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val rates: ExchangeRateRepository,
    reportCurrencies: ReportCurrencySettings,
    private val settings: ConverterSettings,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val moneyFormat: MoneyFormat,
) : ViewModel() {

    /** What is typed and on which side; the pair itself comes from [settings]. */
    private data class Typed(val text: String = "", val active: ConverterSide = ConverterSide.FROM)

    private val typed = MutableStateFlow(Typed())
    private val used = MutableStateFlow<Set<CurrencyCode>?>(null)

    init {
        viewModelScope.launch { used.value = rates.usedCurrencies() }
    }

    val state: StateFlow<ConverterUiState> = combine(
        combine(rates.observeRates(), reportCurrencies.currencies) { all, report -> all to report.first() },
        used.filterNotNull(),
        settings.pick,
        typed,
    ) { (all, base), used, pick, typed -> build(all, base, used, pick, typed) }
        .offMain()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConverterUiState())

    private fun build(all: List<ExchangeRate>, base: CurrencyCode, used: Set<CurrencyCode>, pick: ConverterPick, typed: Typed): ConverterUiState {
        val book = RateBook.of(all)
        val today = today()
        val manual = all.filter { it.origin == RateOrigin.MANUAL }
        val currencies = used + manual.flatMap { listOf(it.from, it.to) }
        val options = listOf(base) + book.currentRates(currencies, base, today).map { it.currency }
        val (from, to) = resolvePair(options, base, pick)
        val input = ConverterInput(from, to, typed.text, typed.active).let { if (pick.mode == ConverterMode.ALL) it.forAll() else it }
        val values = book.converted(keypadMoney(input.text, input.source), options, today, moneyFormat)
        val shown = AmountKeys.display(input.text)
        fun textOf(code: CurrencyCode) = if (code == input.source) shown else values[code]
        return ConverterUiState(
            isLoading = false,
            mode = pick.mode,
            active = input.active,
            from = input.from.value,
            to = input.to.value,
            fromText = textOf(input.from),
            toText = textOf(input.to),
            isEmpty = input.text.isEmpty(),
            rate = book.find(input.from, input.to, today)?.let { formatRate(it.rate) },
            rows = options.map { ConverterRow(it.value, textOf(it), it == input.source) },
            options = options.map { it.value },
            source = input.source.value,
        )
    }

    /** A key of the keypad; fraction digits follow the currency typed in. */
    fun onKey(key: KeypadKey) = change { it.type(key) }

    /** A tap on the side that is not typed on. */
    fun onActivate(side: ConverterSide) = change { it.activate(side) }

    fun onSwap() = change { it.swap() }

    /** A currency chosen for one side of the pair. */
    fun onPick(side: ConverterSide, code: String) = change { it.pick(side, CurrencyCode(code)) }

    /** A tap on a row of the list: count from this currency. */
    fun onSource(code: String) = change { it.source(CurrencyCode(code)) }

    /** The mode button: the pair ↔ the list; remembered. */
    fun onMode(mode: ConverterMode) {
        if (mode == ConverterMode.ALL) change { it.forAll() }
        viewModelScope.launch { settings.update { it.copy(mode = mode) } }
    }

    private fun change(edit: (ConverterInput) -> ConverterInput) {
        val s = state.value
        if (s.isLoading) return
        val before = ConverterInput(CurrencyCode(s.from), CurrencyCode(s.to), typed.value.text, s.active)
        val after = edit(before)
        typed.value = Typed(after.text, after.active)
        if (after.from != before.from || after.to != before.to) {
            viewModelScope.launch { settings.update { it.copy(from = after.from, to = after.to) } }
        }
    }

    private fun today(): LocalDate = clock.todayIn(timeZone)
}
