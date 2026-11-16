package app.coshell.budgets.list

import app.coshell.budgets.list.BudgetsUiState.Caption
import app.coshell.budgets.list.BudgetsUiState.Envelope
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.YearMonth

private fun m(minor: Long, code: String) = MoneyFormat.RU.format(Money(minor, CurrencyCode(code)))
private fun n(minor: Long, code: String) = MoneyFormat.RU.number(Money(minor, CurrencyCode(code)))

// Stress sample of «Бюджеты» (#378): billion-sized limits, 10× overspending, a 40+ character name and a word without
// spaces, a carry-over, a 3-decimal currency and 30 limits in one list — formatted as `BudgetBoard` formats them.
private val stress = listOf(
    Envelope(
        "home", "Коммунальные платежи и обслуживание квартиры", "home", 0xFF8D6E63, 12, n(999_999_999_99, "ARS"), m(100_000_000_00, "ARS"),
        9.99f, 0.37f, BulletTone.DANGER, Caption.Over(m(899_999_999_99, "ARS")), carried = "+" + m(123_456_789_00, "ARS"), hasNote = true,
        children = listOf(
            Envelope(
                "w", "Электроэнергиягазводаиинтернетвместе", "bolt", 0xFFFFC107, null, n(1_234, "KWD"), m(12_345_678, "KWD"),
                0.0001f, 0.37f, BulletTone.NORMAL, Caption.Left(m(12_344_444, "KWD"), m(1_000_000, "KWD")),
            ),
        ),
    ),
    Envelope("zero", "Подарки", "redeem", 0xFFFF9800, null, n(0, "USD"), m(0, "USD"), 0f, 0.37f, BulletTone.IDLE, Caption.Idle),
) + List(30) { i ->
    Envelope(
        "e$i", "Лимит номер ${i + 1}", "shopping_cart", 0xFF8BC34A, null, n(i * 1_000_00L, "ARS"), m(30_000_00, "ARS"), i / 30f, 0.37f,
        BulletTone.NORMAL, Caption.Left(m((30_000 - i * 1_000) * 100L, "ARS"), null),
    )
}

private val stressSummary = BudgetsUiState.Summary(
    left = m(999_999_999_99, "USD"), over = true, spent = n(1_999_999_999_99, "USD"), planned = m(1_000_000_000_00, "USD"),
    fraction = 2f, pace = 0.37f, tone = BulletTone.DANGER, daysLeft = 19,
    forecast = BudgetsUiState.Forecast(m(9_999_999_999_99, "USD"), m(8_999_999_999_99, "USD")),
)

internal val budgetsStressSample =
    BudgetsUiState(month = YearMonth(2026, 11), summary = stressSummary, envelopes = stress, counts = envelopeCounts(stress))
