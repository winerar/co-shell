package app.coshell.reports.recap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.charts.ChartColumn
import app.coshell.core.designsystem.charts.CoShellBarRow
import app.coshell.core.designsystem.charts.CoShellColumns
import app.coshell.core.designsystem.charts.CoShellDonut
import app.coshell.core.designsystem.charts.DonutSlice
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.reports.RecapPhrase
import app.coshell.reports.categoryName

/** The bodies of the stories of [MonthRecapScreen] (boards `RecapS1…S6`); each is a title, one chart or number, a line. */

@Composable
internal fun TopStory(state: MonthRecapUiState, modifier: Modifier) {
    val top = state.top.first()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        StoryTitle(stringResource(R.string.recap_top_title, categoryName(top.category.name)))
        CoShellDonut(
            state.top.map { DonutSlice(it.value, categoryColor(it.category.color)) },
            stringResource(R.string.recap_top_donut),
            Modifier.align(Alignment.CenterHorizontally),
        ) { AmountText("${state.spent} ${state.currency}", AmountTitleStyle) }
        state.top.forEach {
            CoShellBarRow(
                categoryName(it.category.name), it.amount, it.percent / PERCENT_SCALE, categoryColor(it.category.color),
                share = stringResource(R.string.recap_percent, it.percent),
            )
        }
    }
}

@Composable
internal fun VsStory(state: MonthRecapUiState, modifier: Modifier) {
    val change = state.changePercent ?: 0
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        StoryTitle(
            when {
                change <= -SAME -> stringResource(R.string.recap_vs_lighter, -change)
                change >= SAME -> stringResource(R.string.recap_vs_heavier, change)
                else -> stringResource(R.string.recap_vs_same)
            },
        )
        val before = state.previousSpent.orEmpty()
        // The taller column is the bigger month; the two are told apart by their labels, the numbers sit on top.
        val ratio = ratio(state, change)
        CoShellColumns(
            listOf(
                ChartColumn(stringResource(R.string.recap_previous), before, ratio.first),
                ChartColumn(stringResource(R.string.recap_this), state.spent, ratio.second),
            ),
            selected = 1,
        )
    }
}

/** Heights of «previous» and «this» from the signed percent: the bigger month is the full bar. */
private fun ratio(state: MonthRecapUiState, change: Int): Pair<Float, Float> =
    if (change >= 0) (PERCENT_FULL / (PERCENT_FULL + change)) to 1f else 1f to ((PERCENT_FULL + change) / PERCENT_FULL)

@Composable
internal fun WeeksStory(state: MonthRecapUiState, modifier: Modifier) {
    val busiest = state.busiestWeek ?: 0
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        StoryTitle(stringResource(R.string.recap_weeks_title, state.weeks[busiest].label))
        CoShellColumns(state.weeks.map { ChartColumn(it.label, it.amount, it.fraction) }, selected = busiest)
        Caption(stringResource(R.string.recap_weeks_caption))
    }
}

@Composable
internal fun NewStory(state: MonthRecapUiState, modifier: Modifier) {
    val category = state.newCategory ?: return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        StoryTitle(stringResource(R.string.recap_new_title))
        Text(categoryName(category.name), style = MaterialTheme.typography.headlineMedium)
        Caption(stringResource(R.string.recap_new_text))
    }
}

@Composable
internal fun WeekendStory(state: MonthRecapUiState, modifier: Modifier) {
    val weekday = state.weekdayAverage.orEmpty()
    val weekend = state.weekendAverage.orEmpty()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        StoryTitle(stringResource(if (state.weekendMore) R.string.recap_weekend_more else R.string.recap_weekend_less))
        CoShellColumns(
            listOf(
                ChartColumn(stringResource(R.string.recap_weekdays), weekday, if (state.weekendMore) state.weekendRatio else 1f),
                ChartColumn(stringResource(R.string.recap_weekend), weekend, if (state.weekendMore) 1f else state.weekendRatio),
            ),
            selected = 1,
        )
        Caption(stringResource(R.string.recap_weekend_caption))
    }
}

@Composable
internal fun FinalStory(state: MonthRecapUiState, onDetails: () -> Unit, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        StoryTitle(phraseText(state))
        Caption(stringResource(R.string.recap_days, state.recordedDays))
        CoShellButton(onDetails, Modifier.fillMaxWidth().padding(top = Spacing.m)) { Text(stringResource(R.string.recap_details)) }
    }
}

@Composable
private fun phraseText(state: MonthRecapUiState): String = when (state.phrase) {
    RecapPhrase.GOOD_START -> stringResource(R.string.recap_phrase_good_start)
    RecapPhrase.FIRST_MONTH -> stringResource(R.string.recap_phrase_first_month)
    RecapPhrase.LIGHTER -> stringResource(R.string.recap_phrase_lighter, state.phraseNumber)
    RecapPhrase.LIMITS_KEPT -> stringResource(R.string.recap_phrase_limits_kept)
    RecapPhrase.STREAK -> stringResource(R.string.recap_phrase_streak, state.phraseNumber)
    RecapPhrase.SAME_AS_BEFORE -> stringResource(R.string.recap_phrase_same)
    RecapPhrase.SAVED -> stringResource(R.string.recap_phrase_saved, state.phraseNumber)
    RecapPhrase.BUSY_BUT_RECORDED -> stringResource(R.string.recap_phrase_busy)
}

@Composable
private fun Caption(text: String) = Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

private const val SAME = 3
private const val PERCENT_SCALE = 100f
private const val PERCENT_FULL = 100f
