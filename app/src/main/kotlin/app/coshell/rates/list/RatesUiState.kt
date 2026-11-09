package app.coshell.rates.list

import app.coshell.core.domain.rates.DownloadInfo
import app.coshell.core.model.ExchangeRate
import app.coshell.rates.RefreshStatus
import kotlinx.datetime.LocalDate

/** "More → Rates": current rates of the family's currencies, manual rates, "Update now". */
data class RatesUiState(
    val isLoading: Boolean = true,
    /** Currency the current rates are quoted against: `1 base = X`. */
    val base: String = "USD",
    val current: List<CurrentRateItem> = emptyList(),
    val lastDownload: DownloadInfo? = null,
    val manual: List<ManualRateItem> = emptyList(),
    val refresh: RefreshStatus = RefreshStatus.Idle,
    /** Open "manual rate" dialog, `null` when closed. */
    val editor: ManualRateForm? = null,
    /** Manual rate waiting for delete confirmation. */
    val pendingDelete: ManualRateItem? = null,
)

/** One line of "Current rates": `1 base = rateText currency`, or no rate known yet. */
data class CurrentRateItem(
    val currency: String,
    /** "1 049,69"; `null` when no rate is known. */
    val rateText: String?,
    val date: LocalDate?,
    val isManual: Boolean,
    /** Computed through USD (cross rate). */
    val viaPivot: Boolean,
)

/** A rate typed by a family member, e.g. "1 USD = 1 450 ARS" from 5 Nov. */
data class ManualRateItem(val rate: ExchangeRate, val rateText: String)
