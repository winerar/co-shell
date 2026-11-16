package app.coshell.transactions.list

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import app.coshell.transactions.list.TransactionsUiState.Row
import app.coshell.transactions.list.TransactionsUiState.RowTag
import kotlinx.datetime.LocalDate

/**
 * Stress sample of the list (#378): long category, account and tag names, a note, huge amounts in two currencies,
 * and days whose net spans four currencies — formatted as the app formats them, so line breaks match the real list.
 */
internal val transactionsStressSample: TransactionsUiState = run {
    fun m(minor: Long, code: String, plus: Boolean = false) =
        (if (plus) "+" else "") + MoneyFormat.RU.format(Money(minor, CurrencyCode(code)))
    TransactionsUiState(
        days = listOf(
            Day(
                LocalDate(2026, 11, 12), RelativeDay.TODAY,
                listOf(
                    Row(
                        "s1", Kind.EXPENSE, "Коммунальные платежи и обслуживание квартиры", "Тинькофф Накопительный Анастасии",
                        "за октябрь и ноябрь, вместе с перерасчётом", m(-123_456_789_00, "ARS"), m(-98_765_43, "USDT"),
                        icon = "home", color = 0xFF8D6E63, author = "Анастасия Каренина-Толстая",
                        tags = listOf(RowTag("Барилоче и Патагония", 0xFF9BF5D6), RowTag("на двоих с родителями", 0xFFDFFF00)), moreTags = 12,
                    ),
                    Row("s2", Kind.TRANSFER, null, "Тинькофф Накопительный → Тинькофф Black Анастасии", "", m(13_000_000_00, "RUB"), m(14_250_000_00, "AMD", plus = true)),
                    Row("s3", Kind.INCOME, "Зарплата", "Ameria", "", m(9_999_999_999_00, "AMD", plus = true), icon = "work", color = 0xFF43A047),
                ),
                total = listOf(m(-5_000_00, "ARS"), m(-1_93, "USD"), m(250_000_00, "AMD", plus = true), m(-2_000_00, "RUB"))
                    .joinToString(TOTAL_SEPARATOR),
            ),
            Day(
                LocalDate(2025, 11, 11), RelativeDay.OTHER,
                listOf(
                    Row("s4", Kind.EXPENSE, "Еда вне дома", "Песосы", "", m(-5_000_00, "ARS"), icon = "cafe", color = 0xFFFF8A65, author = "Анастасия Каренина"),
                    Row("s5", Kind.EXPENSE, null, "ByBit Саша", "", m(-1_93, "USD")),
                ),
                total = listOf(m(-5_000_00, "ARS"), m(-1_93, "USD")).joinToString(TOTAL_SEPARATOR),
                totalTone = Kind.EXPENSE,
            ),
        ),
    )
}
