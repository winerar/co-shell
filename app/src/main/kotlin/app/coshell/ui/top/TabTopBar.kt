package app.coshell.ui.top

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.AnimatedContent
import app.coshell.core.designsystem.theme.ScreenTitleStyle
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.components.CoShellTopBar
import app.coshell.core.designsystem.components.TopBarScroll
import app.coshell.core.designsystem.components.rememberTopBarScroll
import app.coshell.core.designsystem.components.topBarScroll
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import androidx.compose.ui.semantics.Role
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
 * A narrow floating capsule like the bottom bar (poll 2026-11-16 #441 «Б»). Once the list moves under it, [subtitle] —
 * «where am I», e.g. the day at the top of «Операции» with its net — slides in below the title. On entering a tab the title rolls up
 * from below.
 * [search] puts a loupe among the actions; tapped, the field takes the title's place (#394 Б).
 * [navigation] — a button before the title, [onTitleClick] — the title and its line are a button («Отчёты»: ‹ period ›, #568 «Б»).
 * Only scale, translation and alpha animate (performance.md).
 */
@Composable
fun TabTopBar(
    title: String,
    scroll: TabScroll,
    subtitle: String? = null,
    search: TabSearch? = null,
    navigation: @Composable () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val scrolled = scroll.scrolled
    // A query left from before (rotation, back from an operation) keeps the field open.
    var searching by rememberSaveable { mutableStateOf(search?.query?.isNotEmpty() == true) }
    val enter = remember { Animatable(1f) }
    LaunchedEffect(Unit) { enter.animateTo(0f, Motion.spatial()) }
    run {
        CoShellTopBar(
            title = {
                AnimatedContent(
                    targetState = searching && search != null,
                    transitionSpec = { fadeIn(Motion.effects()).togetherWith(fadeOut(Motion.effectsFast())) },
                    label = "headerSearch",
                ) { open ->
                    if (open && search != null) HeaderSearchField(search, onClose = { searching = false })
                    else TitleBlock(title, subtitle, scrolled, { enter.value }, onTitleClick)
                }
            },
            navigationIcon = {
                val back = LocalTabBack.current
                if (back == null) {
                    navigation()
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BackButton(back)
                        navigation()
                    }
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

/** Title (rolls in on entry) and the «where am I» line under it. */
@Composable
private fun TitleBlock(title: String, subtitle: String?, scrolled: Boolean, enter: () -> Float, onClick: (() -> Unit)?) {
    Column(if (onClick == null) Modifier else Modifier.tactileClickable(Tactile.Control, role = Role.Button, onClick = onClick)) {
        Text(
            title,
            style = ScreenTitleStyle,
            maxLines = 1,
            modifier = Modifier.graphicsLayer {
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

/** Less than a pixel of scroll is still «at the top». */
