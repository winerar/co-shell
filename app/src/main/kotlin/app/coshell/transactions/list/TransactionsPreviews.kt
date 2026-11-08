package app.coshell.transactions.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun TransactionsPreview() {
    CoShellTheme {
        TransactionsScreen(
            TransactionsUiState(
                days = listOf(
                    Day(
                        LocalDate(2026, 11, 8),
                        RelativeDay.TODAY,
                        listOf(
                            Row("1", Kind.EXPENSE, "Кафе", "Карта", "кофе", "-4 500,00 $"),
                            Row("2", Kind.TRANSFER, null, "Наличные → Карта", "", "100,00 US$", "+120 000,00 $"),
                        ),
                    ),
                    Day(
                        LocalDate(2026, 11, 5),
                        RelativeDay.OTHER,
                        listOf(Row("3", Kind.INCOME, "Зарплата", "Ameria", "", "+500 000,00 ֏")),
                    ),
                ),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsEmptyPreview() {
    CoShellTheme { TransactionsScreen(TransactionsUiState()) }
}
