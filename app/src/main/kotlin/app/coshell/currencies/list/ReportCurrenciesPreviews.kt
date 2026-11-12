package app.coshell.currencies.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme

internal val reportCurrenciesPreviewSample = ReportCurrenciesUiState(
    isLoading = false,
    currencies = listOf("USD", "ARS", "RUB"),
    preview = listOf("3\u00A0412,80\u00A0USD", "4\u00A0044\u00A0168,00\u00A0ARS", "277\u00A0802,00\u00A0RUB"),
)

@Preview(showBackground = true)
@Composable
private fun ReportCurrenciesPreview() {
    CoShellTheme {
        ReportCurrenciesScreen(
            reportCurrenciesPreviewSample,
            ReportCurrenciesActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddCurrencyPreview() {
    CoShellTheme {
        ReportCurrenciesScreen(
            ReportCurrenciesUiState(
                isLoading = false,
                currencies = listOf("USD"),
                adding = AddCurrencyForm(code = "ARS", suggestions = listOf("AMD", "ARS", "RUB")),
            ),
            ReportCurrenciesActions(),
        )
    }
}
