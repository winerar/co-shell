package app.coshell.transactions.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import app.coshell.transactions.list.TransactionsUiState.Row
import app.coshell.core.domain.search.PeriodRange
import kotlinx.datetime.LocalDate

internal val transactionsPreviewSample =
    TransactionsUiState(
        days = listOf(
            Day(
                LocalDate(2026, 11, 8),
                RelativeDay.TODAY,
                listOf(
                    Row("1", Kind.EXPENSE, "Кафе", "Карта", "кофе", "-4 500,00 ARS"),
                    Row("4", Kind.INCOME, "Кафе", "Карта", "", "+1 200,00 ARS", refund = true),
                    Row("2", Kind.TRANSFER, null, "Наличные → Карта", "", "100,00 USD", "+120 000,00 ARS"),
                ),
            ),
            Day(
                LocalDate(2026, 11, 5),
                RelativeDay.OTHER,
                listOf(Row("3", Kind.INCOME, "Зарплата", "Ameria", "", "+500 000,00 AMD")),
            ),
        ),
    )

/** Board `Fb141A`: food categories, October, Аня — and the total under the chips (4А). */
internal val transactionsFilterChoices = FilterChoices(
    accounts = listOf(
        FilterChoices.AccountChoice("galicia", "Galicia", "ARS", 0xFFF26E22),
        FilterChoices.AccountChoice("mp", "Mercado Pago", "ARS", 0xFF00B1EA),
        FilterChoices.AccountChoice("ameria", "Ameria", "AMD", null),
        FilterChoices.AccountChoice("usdt", "USDT-карта", "USDT", 0xFF26A17B),
        FilterChoices.AccountChoice("cash", "Наличные USD", "USD", null),
    ),
    expenseCategories = listOf(
        FilterChoices.CategoryChoice(
            "food", "Еда", "cart", null,
            children = listOf(
                FilterChoices.CategoryChoice("shop", "Продукты", "cart", null),
                FilterChoices.CategoryChoice("bakery", "Пекарня", "cart", null),
                FilterChoices.CategoryChoice("delivery", "Доставка", "cart", null),
                FilterChoices.CategoryChoice("cafe", "Кафе", "cafe", null),
                FilterChoices.CategoryChoice("sweets", "Сладкое", "cart", null),
            ),
        ),
        FilterChoices.CategoryChoice(
            "home", "Дом", "home", null,
            children = listOf(FilterChoices.CategoryChoice("rent", "Аренда", "home", null), FilterChoices.CategoryChoice("power", "Свет", "home", null)),
        ),
        FilterChoices.CategoryChoice("taxi", "Такси", "taxi", null),
        FilterChoices.CategoryChoice("pharmacy", "Аптека", "pill", null),
    ),
    incomeCategories = listOf(FilterChoices.CategoryChoice("salary", "Зарплата", "work", null)),
    members = listOf(FilterChoices.MemberChoice("anya", "Аня")),
)

internal val transactionsFilteredSample = transactionsPreviewSample.copy(
    filter = ListFilter(
        kind = Kind.EXPENSE,
        categoryIds = setOf("shop", "bakery", "delivery"),
        period = PeriodRange.Preset.LAST_MONTH.range(LocalDate(2026, 11, 12)),
        who = ListFilter.Who.Member("anya"),
    ),
    choices = transactionsFilterChoices,
    total = TransactionsUiState.Total(23, "-184 300,00 ARS", "≈ -182,40 USD", Kind.EXPENSE),
    today = LocalDate(2026, 11, 12),
)

@Preview(showBackground = true)
@Composable
private fun TransactionsPreview() {
    CoShellTheme { TransactionsScreen(transactionsPreviewSample) }
}

/** Mockup 19В: two rows chosen by a long press. */
@Preview(showBackground = true)
@Composable
private fun TransactionsSelectionPreview() {
    CoShellTheme { TransactionsScreen(transactionsPreviewSample, selection = Selection(setOf("1", "3"))) }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsEmptyPreview() {
    CoShellTheme { TransactionsScreen(TransactionsUiState()) }
}
