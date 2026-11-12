package app.coshell.budgets.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.budgets.list.BudgetsUiState.Caption
import app.coshell.budgets.list.BudgetsUiState.Envelope
import app.coshell.core.designsystem.components.BulletTone
import app.coshell.core.designsystem.theme.CoShellTheme
import kotlinx.datetime.YearMonth

// Numbers of mockup `BudA`.
private val sample = listOf(
    Envelope(
        "cafe", "Кафе", "restaurant", 0xFFE91E63, null, "95 000", "80 000 ARS", 1.19f, 0.37f, BulletTone.DANGER,
        Caption.Over("15 000 ARS"),
    ),
    Envelope(
        "home", "Дом", "home", 0xFF00BCD4, 3, "310 000", "400 000 ARS", 0.78f, 0.37f, BulletTone.WARNING,
        Caption.Ahead("90 000 ARS", 19),
        children = listOf(
            Envelope("utilities", "Коммуналка", "bolt", 0xFF00BCD4, null, "30 000", "70 000 ARS", 0.43f, 0.37f, BulletTone.NORMAL, Caption.Left("40 000 ARS", null)),
        ),
    ),
    Envelope(
        "food", "Продукты", "shopping_cart", 0xFF8BC34A, null, "130 000", "350 000 ARS", 0.37f, 0.37f, BulletTone.NORMAL,
        Caption.Left("220 000 ARS", "11 600 ARS"),
    ),
    Envelope("gifts", "Подарки", "redeem", 0xFFFF9800, null, "0", "50 USD", 0f, 0.37f, BulletTone.IDLE, Caption.Idle),
)

private val summary = BudgetsUiState.Summary(
    left = "540 USD", over = false, spent = "640", planned = "1 180 USD", fraction = 0.54f, pace = 0.37f,
    tone = BulletTone.WARNING, daysLeft = 19, forecast = BudgetsUiState.Forecast("1 260 USD", "80 USD"),
)

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun BudgetsPreview() {
    CoShellTheme {
        BudgetsScreen(
            BudgetsUiState(month = YearMonth(2026, 11), summary = summary, envelopes = sample, counts = envelopeCounts(sample)),
            BudgetsActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgetsEmptyPreview() {
    CoShellTheme { BudgetsScreen(BudgetsUiState(month = YearMonth(2026, 11)), BudgetsActions()) }
}
