package app.coshell.release

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.fillEdgeColor
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.recap.storyTransition
import app.coshell.ui.bar.aboveBottomBars
import app.coshell.ui.top.ScreenTopBar

/**
 * «Что нового · 26.12.1» as stories (#693, boards `Rel693S1…S5`; the card look of #598 «А»): bars on top, a scheme, a title and one or two
 * sentences per story; a tap on the card or «Дальше» goes on, «Готово» on the last one closes. The same springs as «Итоги месяца».
 */
@Composable
fun ReleaseStoriesScreen(
    versionName: String,
    stories: List<ReleaseStory>,
    onClose: () -> Unit = {},
    startAt: Int = 0,
) {
    var index by rememberSaveable { mutableIntStateOf(startAt) }
    val last = stories.lastIndex
    val at = index.coerceIn(0, maxOf(last, 0))
    val next = { if (at < last) index = at + 1 else onClose() }
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.release_header, versionName), onBack = onClose) },
        bottomBar = {
            if (stories.isNotEmpty()) {
                CoShellButton(onClick = next, modifier = Modifier.fillMaxWidth().aboveBottomBars().padding(Spacing.l)) {
                    Text(stringResource(if (at < last) R.string.release_next else R.string.release_done))
                }
            }
        },
    ) { padding ->
        if (stories.isEmpty()) return@Scaffold
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = Spacing.l)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                stories.indices.forEach { i -> CoShellMeter(if (i <= at) 1f else 0f, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) }
            }
            val description = stringResource(R.string.release_progress, at + 1, stories.size)
            CoShellCard(
                Modifier.fillMaxWidth().weight(1f).padding(top = Spacing.m).semantics { contentDescription = description },
                tone = CardTone.Story,
                onClick = next,
            ) {
                AnimatedContent(at, transitionSpec = { storyTransition(last) }, label = "release-story") { shown ->
                    StoryBody(stories[shown.coerceIn(0, last)], Modifier.fillMaxSize().padding(Spacing.l))
                }
            }
        }
    }
}

@Composable
private fun StoryBody(story: ReleaseStory, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        StoryScheme(story, Modifier.fillMaxWidth().padding(vertical = Spacing.xl))
        Text(stringResource(story.title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(story.text), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** The three layers of a scheme in the theme's colours: the main colour, its hairline where it melts into the card, the lines on top. */
@Composable
private fun StoryScheme(story: ReleaseStory, modifier: Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val edge = fillEdgeColor(accent, MaterialTheme.colorScheme.surface) ?: Color.Transparent
    val layer = Modifier.fillMaxWidth().aspectRatio(SCHEME_RATIO)
    Box(modifier) {
        Icon(painterResource(story.accent), contentDescription = null, tint = accent, modifier = layer)
        Icon(painterResource(story.edge), contentDescription = null, tint = edge, modifier = layer)
        Icon(painterResource(story.ink), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = layer)
    }
}

/** The schemes are drawn on 300 × 180 (`scripts/agent/story_art_693.py`). */
private const val SCHEME_RATIO = 300f / 180f
