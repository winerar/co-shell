package app.coshell.rates.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.rates.DownloadInfo
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.rates.RefreshStatus
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

private val nov9 = LocalDate(2026, 11, 9)

internal val ratesPreviewSample = RatesUiState(
    isLoading = false,
    current = listOf(
        CurrentRateItem("AMD", "387,15", nov9, isManual = false, viaPivot = false, converted = "38 715"),
        CurrentRateItem("ARS", "1 450", LocalDate(2026, 11, 5), isManual = true, viaPivot = false, converted = "145 000"),
        CurrentRateItem("RUB", null, null, isManual = false, viaPivot = false),
    ),
    lastDownload = DownloadInfo(nov9, "currency-api/jsdelivr", Instant.parse("2026-11-09T12:00:00Z")),
    manual = listOf(
        ManualRateItem(
            ExchangeRate(LocalDate(2026, 11, 5), CurrencyCode("USD"), CurrencyCode("ARS"), Rate.parse("1450"), RateOrigin.MANUAL),
            "1 450",
        ),
    ),
    refresh = RefreshStatus.Updated,
    converter = ConverterUi(amountText = "100", from = "USD", options = listOf("USD", "AMD", "ARS", "RUB")),
)

/** 16 currencies — enough to scroll the screen under its header (#394 Б: the hairline). */
internal val ratesLongSample = ratesPreviewSample.copy(
    current = listOf("AMD", "ARS", "RUB", "USDT", "EUR", "GEL", "TRY", "BRL", "CLP", "UYU", "KZT", "GBP", "CNY", "AED", "ILS", "PLN").mapIndexed { i, code ->
        CurrentRateItem(code, "${100 + i * 37},5", nov9, isManual = false, viaPivot = false, converted = "${(100 + i * 37) * 100}")
    },
)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RatesPreview() {
    CoShellTheme { RatesScreen(ratesPreviewSample, RatesActions()) }
}

@Preview(showBackground = true)
@Composable
private fun RatesEditorPreview() {
    CoShellTheme {
        RatesScreen(ratesPreviewSample.copy(editor = ManualRateForm("USD", "ARS", "1 450", nov9, nov9)), RatesActions())
    }
}
