package app.coshell.currencies.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme

@Preview(showBackground = true)
@Composable
private fun ReportCurrenciesPreview() {
    CoShellTheme {
        ReportCurrenciesScreen(
            ReportCurrenciesUiState(isLoading = false, currencies = listOf("USD", "ARS", "AMD")),
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
