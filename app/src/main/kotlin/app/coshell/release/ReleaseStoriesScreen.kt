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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.components.CardTone
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.fillEdgeColor
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.recap.StoryTap
import app.coshell.reports.recap.storyTaps
import app.coshell.reports.recap.storyTransition
import app.coshell.ui.bar.aboveBottomBars
import app.coshell.ui.top.ScreenTopBar

/**
 * «Что нового · 26.12.1» as stories (#693, boards `Rel693S1…S5`; the card look of #598 «А»): bars on top, a scheme, a title and one or two
 * sentences per story; a tap on the card or «Дальше» goes on, a tap on its left third or a swipe right goes back, «Готово» on the last one closes. The same springs as «Итоги месяца».
 */
@Composable
fun ReleaseStoriesScreen(versionName: String, stories: List<ReleaseStory>, onClose: () -> Unit = {}, startAt: Int = 0) =
    StoriesScreen(stringResource(R.string.release_header, versionName), stories, stringResource(R.string.release_done), onClose, startAt)

/**
 * The stories engine of «Что нового» and «Знакомство» (#598): [header] in the top bar, [doneLabel] on the last button,
 * [steps] — «Шаг N из M» over the title (the tour is an order of things to do).
 */
@Composable
fun StoriesScreen(
    header: String,
    stories: List<ReleaseStory>,
    doneLabel: String,
    onClose: () -> Unit = {},
    startAt: Int = 0,
    steps: Boolean = false,
) {
    var index by rememberSaveable { mutableIntStateOf(startAt) }
    val last = stories.lastIndex
    val at = index.coerceIn(0, maxOf(last, 0))
    var dir by remember { mutableIntStateOf(1) }
    val go = { step: Int ->
        when {
            step < 0 -> if (at > 0) { dir = -1; index = at - 1 }
            at < last -> { dir = 1; index = at + 1 }
            else -> onClose()
        }
    }
    val next = { go(1) }
    val tap = remember { StoryTap() }
    val previous = stringResource(R.string.story_previous)
    Scaffold(
        topBar = { ScreenTopBar(header, onBack = onClose) },
        bottomBar = {
            if (stories.isNotEmpty()) {
                CoShellButton(onClick = next, modifier = Modifier.fillMaxWidth().aboveBottomBars().padding(Spacing.l)) {
                    Text(if (at < last) stringResource(R.string.release_next) else doneLabel)
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
                Modifier.fillMaxWidth().weight(1f).padding(top = Spacing.m).storyTaps(tap, go).semantics {
                    contentDescription = description
                    if (at > 0) customActions = listOf(CustomAccessibilityAction(previous) { go(-1); true })
                },
                tone = CardTone.Story,
                onClick = { go(tap.step) },
            ) {
                AnimatedContent(at, transitionSpec = { storyTransition(dir) }, label = "release-story") { shown ->
                    val i = shown.coerceIn(0, last)
                    StoryBody(stories[i], if (steps) stringResource(R.string.tour_step, i + 1, stories.size) else null, Modifier.fillMaxSize().padding(Spacing.l))
                }
            }
        }
    }
}

@Composable
private fun StoryBody(story: ReleaseStory, step: String?, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        StoryScheme(story, Modifier.fillMaxWidth().padding(vertical = Spacing.xl))
        Column {
            step?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(stringResource(story.title), style = MaterialTheme.typography.headlineSmall)
        }
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
