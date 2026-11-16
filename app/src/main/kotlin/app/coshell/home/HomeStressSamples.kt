package app.coshell.home

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.AccountType
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.LocalDate

/**
 * Stress sample of «Обзор» (#378): eleven currencies with 0, 2, 3 and 8 (BTC) decimals, a billion-sized and a negative total,
 * a zero balance, long account and category names and a word without spaces — formatted as the app formats them.
 */
internal val homeStressSample: HomeUiState = run {
    fun m(minor: Long, code: String, plus: Boolean = false) =
        (if (plus) "+" else "") + MoneyFormat.RU.format(Money(minor, CurrencyCode(code)))
    val many = listOf(
        m(999_999_999_99, "ARS"), m(-1_234_567_89, "USD"), m(98_765_432_10, "AMD"), m(105_800_50, "RUB"),
        m(1_234_567, "JPY"), m(12_345_678, "KWD"), m(12_345_678_901, "BTC"), m(0, "EUR"),
        m(77_777_77, "BRL"), m(5_000_000_00, "CLP"), m(42_00, "GEL"),
    )
    HomeUiState(
        totals = listOf(m(-999_999_999_99, "USD"), m(999_999_999_99, "ARS")),
        rateDate = LocalDate(2026, 11, 9),
        byCurrency = many,
        spending = HomeUiState.MonthSpending(
            month = LocalDate(2026, 11, 1),
            totals = listOf(m(999_999_999_99, "USD"), m(999_999_999_99, "ARS"), m(1_234_567, "JPY")),
            missing = listOf("VES", "KWD"),
            daily = listOf(99_999_999L, 0, 1, 800, 3_000, 0, 0, 99_999_999, 2_500) + List(21) { 0L },
            today = 8,
        ),
        income = HomeUiState.MonthTotals(listOf(m(9_999_999_999_00, "AMD", plus = true), m(12_345_678, "KWD", plus = true))),
        attention = HomeUiState.Attention(missingRates = listOf("VES", "KWD", "USDT"), uncategorized = 1_234),
        accounts = listOf(
            HomeUiState.AccountRow("1", "Тинькофф Накопительный Анастасии для отпуска", m(999_999_999_99, "RUB"), isNegative = false),
            HomeUiState.AccountRow("2", "Кредиткавдолларахбезпробеловвообще", m(-999_999_999_99, "USD"), isNegative = true, excludedFromTotal = true),
            HomeUiState.AccountRow("3", "Пусто", m(0, "ARS"), isNegative = false, type = AccountType.CASH),
            HomeUiState.AccountRow("4", "ByBit", m(12_345_678_901, "BTC"), isNegative = false, type = AccountType.CRYPTO),
        ),
        recent = listOf(
            Row(
                "r1", Kind.EXPENSE, "Коммунальные платежи и обслуживание квартиры", "Тинькофф Накопительный Анастасии",
                "за октябрь и ноябрь, вместе с перерасчётом", m(-123_456_789_00, "ARS"), m(-98_765_43, "USDT"),
                icon = "home", color = 0xFF8D6E63,
            ),
            Row("r2", Kind.INCOME, "Зарплатанадевятьмиллиардовдрамов", "Ameria", "", m(9_999_999_999_00, "AMD", plus = true), icon = "work", color = 0xFF43A047),
            Row("r3", Kind.TRANSFER, null, "Тинькофф Накопительный → Тинькофф Black Анастасии", "", m(12_345_678, "KWD")),
        ),
    )
}
