package app.coshell.ui.top

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes

/** Scroll link between a tab's list and its [TabTopBar]; hides Material's experimental type from screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class TabScroll internal constructor(internal val behavior: TopAppBarScrollBehavior) {
    /** True once the list has moved under the header: the header shrinks its title and draws a line. */
    val scrolled: Boolean get() = behavior.state.overlappedFraction > SCROLLED_FRACTION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberTabScrollBehavior(): TabScroll {
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()
    return remember(behavior) { TabScroll(behavior) }
}

/** Lets the list's scroll reach the [TabTopBar]; put it on the screen's Scaffold. */
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.tabScroll(scroll: TabScroll): Modifier = nestedScroll(scroll.behavior.nestedScrollConnection)

/**
 * Header of a tab screen (Обзор, Операции, Отчёты, Ещё) — «Живая строка», option А of the header poll
 * (2026-11-15, owner: «без эффекта прозрачности»). One compact row with a bold title; the tall collapsing
 * header is gone for good (owner, 2026-11-10).
 *
 * At rest it melts into the screen. Once the list moves under it, the header stays solid (same colour as the
 * screen, no tint or glass), draws a hairline, the title shrinks and [subtitle] — «where am I», e.g. the day
 * at the top of «Операции» with its net — slides in below. On entering a tab the title rolls up from below.
 * Only scale, translation and alpha animate (performance.md).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabTopBar(
    title: String,
    scroll: TabScroll,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val scrolled = scroll.scrolled
    val scale by animateFloatAsState(if (scrolled) SCROLLED_TITLE_SCALE else 1f, Motion.spatial(), label = "titleScale")
    val enter = remember { Animatable(1f) }
    LaunchedEffect(Unit) { enter.animateTo(0f, Motion.spatial()) }
    val surface = MaterialTheme.colorScheme.surface
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val line by animateFloatAsState(if (scrolled) 1f else 0f, Motion.effects(), label = "hairline")
    // The hairline is drawn over the bar's bottom edge, so the header keeps its height and nothing shifts.
    Column(
        Modifier.drawWithContent {
            drawContent()
            val y = size.height - Sizes.hairline.toPx() / 2
            drawLine(lineColor.copy(alpha = line), Offset(0f, y), Offset(size.width, y), Sizes.hairline.toPx())
        },
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.graphicsLayer {
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            scaleX = scale
                            scaleY = scale
                            translationY = enter.value * size.height
                            alpha = 1f - enter.value
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
            },
            actions = actions,
            scrollBehavior = scroll.behavior,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = surface, scrolledContainerColor = surface),
        )
    }
}

/** The title at 74 % once scrolled: 28 sp → ≈ 21 sp, the size of an inner screen's title. */
private const val SCROLLED_TITLE_SCALE = 0.74f
private const val SCROLLED_FRACTION = 0.01f
