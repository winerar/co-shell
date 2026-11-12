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
        CurrentRateItem("AMD", "387,15", nov9, isManual = false, viaPivot = false),
        CurrentRateItem("ARS", "1 450", LocalDate(2026, 11, 5), isManual = true, viaPivot = false),
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
