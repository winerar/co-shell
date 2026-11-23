package app.coshell.reports.recap

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.reports.RecapCard
import app.coshell.ui.format.formatMonthOf
import app.coshell.ui.top.ScreenTopBar

/**
 * «Итоги октября» as stories (#554, owner poll 2026-11-23, all «А»): a hero card per story, bars on top show where
 * you are, a tap anywhere on the card goes on, the last story ends with an encouraging phrase and «Подробнее».
 * One card is always on screen; a month with nothing in it has no stories.
 */
@Composable
fun MonthRecapScreen(state: MonthRecapUiState, onBack: () -> Unit = {}, onDetails: () -> Unit = {}, startAt: Int = 0) {
    val month = state.month
    Scaffold(topBar = { ScreenTopBar(month?.let { stringResource(R.string.recap_title, formatMonthOf(it)) }.orEmpty(), onBack = onBack) }) { padding ->
        if (state.isLoading || month == null) return@Scaffold
        val side = Modifier.padding(horizontal = Spacing.l)
        if (state.cards.isEmpty()) {
            CoShellMessage(stringResource(R.string.recap_empty), isError = false, modifier = side.padding(padding).padding(top = Spacing.l))
            return@Scaffold
        }
        var index by rememberSaveable { mutableIntStateOf(startAt) }
        val last = state.cards.lastIndex
        val at = index.coerceIn(0, last)
        Column(Modifier.fillMaxSize().padding(padding).then(side)) {
            Row(
                Modifier.fillMaxWidth().semantics { contentDescription = "" },
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                state.cards.indices.forEach { i ->
                    CoShellMeter(if (i <= at) 1f else 0f, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                }
            }
            val description = stringResource(R.string.recap_progress, at + 1, state.cards.size)
            CoShellCard(
                Modifier.fillMaxWidth().padding(top = Spacing.m).semantics { contentDescription = description },
                tone = CardTone.Hero,
                onClick = { index = if (at < last) at + 1 else 0 },
            ) {
                AnimatedContent(at, transitionSpec = { storyTransition(last) }, label = "story") { shown ->
                    Story(state.cards[shown.coerceIn(0, last)], state, onDetails, Modifier.fillMaxSize().padding(Spacing.l))
                }
            }
        }
    }
}

@Composable
private fun Story(card: RecapCard, state: MonthRecapUiState, onDetails: () -> Unit, modifier: Modifier) = when (card) {
    RecapCard.TOP_CATEGORIES -> TopStory(state, modifier)
    RecapCard.VS_PREVIOUS -> VsStory(state, modifier)
    RecapCard.WEEKS -> WeeksStory(state, modifier)
    RecapCard.NEW_CATEGORY -> NewStory(state, modifier)
    RecapCard.WEEKEND -> WeekendStory(state, modifier)
    RecapCard.FINAL -> FinalStory(state, onDetails, modifier)
}

@Composable
internal fun StoryTitle(text: String) = Text(text, style = MaterialTheme.typography.titleLarge)
