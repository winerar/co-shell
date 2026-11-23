package app.coshell.reports.recap

import app.coshell.core.domain.reports.RecapCard
import app.coshell.core.domain.reports.RecapPhrase
import kotlinx.datetime.YearMonth

/** October 2026 as on the boards `Rec554*`: spent 1 140 USD against 1 320 in September. */
internal object MonthRecapSamples {
    private fun named(id: String, name: String, color: Long) = MonthRecapUiState.NamedCategory(id, name, color)

    val state = MonthRecapUiState(
        month = YearMonth(2026, 10),
        currency = "USD",
        spent = "1 140",
        cards = RecapCard.entries,
        top = listOf(
            MonthRecapUiState.Share(named("food", "Продукты", 0xFF7FD6A0), "410", 36, 41_000),
            MonthRecapUiState.Share(named("home", "Дом", 0xFF6BB8FF), "380", 33, 38_000),
            MonthRecapUiState.Share(named("cafe", "Кафе", 0xFFFFB86B), "120", 11, 12_000),
            MonthRecapUiState.Share(named("taxi", "Такси", 0xFFE58BD0), "70", 6, 7_000),
        ),
        previousSpent = "1 320",
        changePercent = -14,
        weeks = listOf(
            MonthRecapUiState.Week("1–7", "220", 0.34f),
            MonthRecapUiState.Week("8–14", "640", 1f),
            MonthRecapUiState.Week("15–21", "180", 0.28f),
            MonthRecapUiState.Week("22–28", "150", 0.23f),
            MonthRecapUiState.Week("29–31", "60", 0.09f),
        ),
        busiestWeek = 1,
        newCategory = named("gifts", "Подарки", 0xFFFFE14D),
        weekdayAverage = "57",
        weekendAverage = "100",
        weekendMore = true,
        weekendRatio = 0.57f,
        phrase = RecapPhrase.LIGHTER,
        phraseNumber = 14,
        recordedDays = 22,
        streak = 13,
        streakFrom = 7,
        streakTo = 19,
    )
}
