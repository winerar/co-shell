package app.coshell.debts.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme

/** Board `DebtList`: a partly returned debt, one without a day, one «soon», one late; two closed ones folded. */
internal fun debtsPreviewSample(): List<DebtRowUi> = listOf(
    DebtRowUi("1", "Саша", "120 USD", "300 USD", 0.6f, DebtDue.SET, "5 декабря", 12, "за билеты", false),
    DebtRowUi("2", "Мама", "45 000 RUB", "", 0f, DebtDue.NONE, "", 0, "", false),
    DebtRowUi("3", "Игорь", "80 USD", "", 0f, DebtDue.SOON, "26 ноября", 3, "за ремонт", false),
    DebtRowUi("4", "Лена", "15 000 ARS", "", 0f, DebtDue.LATE, "19 ноября", 4, "за обед", false),
)

internal fun debtsPreviewState() = DebtsUiState(
    isLoading = false,
    totals = listOf("200 USD", "45 000 RUB", "15 000 ARS"),
    active = debtsPreviewSample(),
    closed = listOf(DebtRowUi("5", "Дима", "0 USD", "50 USD", 1f, DebtDue.NONE, "", 0, "", true)),
    nothingAtAll = false,
)

@Preview(showBackground = true)
@Composable
private fun DebtsScreenPreview() {
    CoShellTheme { DebtsScreen(debtsPreviewState(), DebtsActions()) }
}

@Preview(showBackground = true)
@Composable
private fun DebtsScreenEmptyPreview() {
    CoShellTheme(darkTheme = true) { DebtsScreen(DebtsUiState(isLoading = false), DebtsActions()) }
}
