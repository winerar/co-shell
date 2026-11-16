package app.coshell.ui.top

import androidx.compose.animation.AnimatedContent
import app.coshell.core.designsystem.theme.LargeTitleStyle
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.components.CoShellTopBar
import app.coshell.core.designsystem.components.TopBarScroll
import app.coshell.core.designsystem.components.rememberTopBarScroll
import app.coshell.core.designsystem.components.topBarScroll
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.tokens.Motion

typealias TabScroll = TopBarScroll

@Composable
fun rememberTabScrollBehavior(): TabScroll = rememberTopBarScroll()

/** Lets the list's scroll reach the [TabTopBar]; put it on the screen's Scaffold. */
fun Modifier.tabScroll(scroll: TabScroll): Modifier = topBarScroll(scroll)

/**
 * Header of a tab screen (Обзор, Операции, Отчёты, Ещё) — «Живая строка», option А of the header poll
 * (2026-11-15, owner: «без эффекта прозрачности»). One compact row with a bold title; the tall collapsing
 * header is gone for good (owner, 2026-11-10).
 *
 * At rest it melts into the screen. Once the list moves under it, the header stays solid (same colour as the
 * screen, no tint or glass), draws a hairline, the title shrinks and [subtitle] — «where am I», e.g. the day
 * at the top of «Операции» with its net — slides in below. On entering a tab the title rolls up from below.
 * [search] puts a loupe among the actions; tapped, the field takes the title's place (#394 Б).
 * Only scale, translation and alpha animate (performance.md).
 */
@Composable
fun TabTopBar(
    title: String,
    scroll: TabScroll,
    subtitle: String? = null,
    search: TabSearch? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val scrolled = scroll.scrolled
    // A query left from before (rotation, back from an operation) keeps the field open.
    var searching by rememberSaveable { mutableStateOf(search?.query?.isNotEmpty() == true) }
    val scale by animateFloatAsState(if (scrolled) SCROLLED_TITLE_SCALE else 1f, Motion.spatial(), label = "titleScale")
    val enter = remember { Animatable(1f) }
    LaunchedEffect(Unit) { enter.animateTo(0f, Motion.spatial()) }
    Column(Modifier.scrollHairline(scrolled)) {
        CoShellTopBar(
            title = {
                AnimatedContent(
                    targetState = searching && search != null,
                    transitionSpec = { fadeIn(Motion.effects()).togetherWith(fadeOut(Motion.effectsFast())) },
                    label = "headerSearch",
                ) { open ->
                    if (open && search != null) HeaderSearchField(search, onClose = { searching = false })
                    else TitleBlock(title, subtitle, scrolled, { scale }, { enter.value })
                }
            },
            actions = {
                TopBarActions {
                    if (search != null) {
                        if (searching) SearchCloseButton(search, onClose = { searching = false })
                        else SearchOpenButton(search, onOpen = { searching = true })
                    }
                    actions()
                }
            },
            scroll = scroll,
        )
    }
}

/** Title (shrinks once scrolled, rolls in on entry) and the «where am I» line under it. */
@Composable
private fun TitleBlock(title: String, subtitle: String?, scrolled: Boolean, scale: () -> Float, enter: () -> Float) {
    Column {
        Text(
            title,
            style = LargeTitleStyle,
            maxLines = 1,
            modifier = Modifier.graphicsLayer {
                transformOrigin = TransformOrigin(0f, 0.5f)
                scaleX = scale()
                scaleY = scale()
                translationY = enter() * size.height
                alpha = 1f - enter()
            },
        )
        CoShellExpandable(expanded = scrolled && subtitle != null) {
            Text(
                subtitle.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The title at 74 % once scrolled: 28 sp → ≈ 21 sp, the size of an inner screen's title. */
private const val SCROLLED_TITLE_SCALE = 0.74f
/** Less than a pixel of scroll is still «at the top». */
