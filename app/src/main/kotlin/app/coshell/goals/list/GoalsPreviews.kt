package app.coshell.goals.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.goals.GoalRowUi
import app.coshell.goals.GoalTotalsUi
import app.coshell.goals.GoalWhen
import app.coshell.goals.edit.GoalEditScreen
import app.coshell.goals.edit.GoalEditActions
import app.coshell.goals.edit.GoalEditUiState
import app.coshell.goals.edit.GoalProgressUi
import app.coshell.core.designsystem.components.RibbonAccount

/** Board `GoalListA`: a trip with a deadline, a yearly insurance, courses in pesos, a bike without a deadline; a reached cushion folded. */
internal fun goalsPreviewSample(): List<GoalRowUi> = listOf(
    GoalRowUi("1", "Отпуск", "travel", 0xFF7FD6A0, "1 250 USD", "2 000 USD", "750 USD", 0.625f, GoalWhen.DEADLINE, "1 июля", "125 USD", "Копилка USD"),
    GoalRowUi("2", "Страховка квартиры", "home", 0xFFFFB86B, "180 USD", "360 USD", "180 USD", 0.5f, GoalWhen.YEARLY, "15 марта", "45 USD", "Копилка USD"),
    GoalRowUi("3", "Курсы английского", "education", 0xFF6BB8FF, "240 000 ARS", "600 000 ARS", "360 000 ARS", 0.4f, GoalWhen.DEADLINE, "1 февраля", "180 000 ARS", "Galicia"),
    GoalRowUi("4", "Новый велосипед", null, null, "40 USD", "450 USD", "410 USD", 0.09f, GoalWhen.NO_DEADLINE, "", "", "Наличные"),
)

internal fun goalsPreviewState() = GoalsUiState(
    isLoading = false,
    totals = GoalTotalsUi("1 635 USD", "350 USD", ""),
    active = goalsPreviewSample(),
    reached = listOf(GoalRowUi("5", "Подушка", null, 0xFFE58BD0, "3 000 USD", "3 000 USD", "0 USD", 1f, GoalWhen.REACHED, "", "", "Вклад")),
)

internal fun goalEditPreviewState(existing: Boolean = false) = GoalEditUiState(
    isNew = !existing,
    isLoading = false,
    name = "Отпуск",
    amount = "2000",
    currency = "USD",
    accountId = "box",
    ribbon = listOf(RibbonAccount("box", "Копилка USD", 0xFF7FD6A0), RibbonAccount("dep", "Вклад", 0xFFFFB86B), RibbonAccount("cash", "Наличные", 0xFF6BB8FF)),
    deadlineMillisUtc = 1_814_400_000_000,
    deadlineText = "1 июля 2027",
    planPerMonth = "125 USD",
    planDeadline = "1 июля",
    progress = if (existing) GoalProgressUi("1 250 USD", "2 000 USD", "750 USD", 0.625f, reached = false) else null,
)

@Preview(showBackground = true)
@Composable
private fun GoalsScreenPreview() {
    CoShellTheme { GoalsScreen(goalsPreviewState(), GoalsActions()) }
}

@Preview(showBackground = true)
@Composable
private fun GoalsScreenEmptyPreview() {
    CoShellTheme(darkTheme = true) { GoalsScreen(GoalsUiState(isLoading = false), GoalsActions()) }
}

@Preview(showBackground = true)
@Composable
private fun GoalEditPreview() {
    CoShellTheme { GoalEditScreen(goalEditPreviewState(existing = true), GoalEditActions()) }
}
