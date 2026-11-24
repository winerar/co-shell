package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import app.coshell.core.designsystem.theme.panelColor
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Scroll link between a screen's list and its [CoShellTopBar]; hides Material's experimental type from screens.
 * Make one with [rememberTopBarScroll], put [topBarScroll] on the Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class TopBarScroll internal constructor(internal val behavior: TopAppBarScrollBehavior) {
    /**
     * True once the list has moved under the bar: the bar draws a line and shrinks its title. Read from the scrolled
     * distance, not `overlappedFraction`: that one needs a bar bound to the behaviour, and an inner screen has none.
     */
    val scrolled: Boolean get() = behavior.state.contentOffset < -SCROLLED_PX
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberTopBarScroll(): TopBarScroll {
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()
    return remember(behavior) { TopBarScroll(behavior) }
}

/** Lets the list's scroll reach the [CoShellTopBar]. */
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.topBarScroll(scroll: TopBarScroll): Modifier = nestedScroll(scroll.behavior.nestedScrollConnection)

/**
 * Screen top bar (board `CompNew`, «Шапка экрана», #367 А): one compact row of [navigationIcon], [title] and [actions].
 * A floating capsule on the screen's background (the same style as the bottom bar), solid fill, never tinted or glass; [raised] —
 * the selection bar's brighter fill, so the mode is seen at a glance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoShellTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scroll: TopBarScroll? = null,
    raised: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val fill = if (raised) scheme.surfaceContainerHighest else panelColor()
    // A narrow floating capsule, like the bottom bar (poll 2026-11-16 #441 «Б»): nothing behind it but the screen.
    Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Sizes.panelInset, vertical = Spacing.s)) {
        TopAppBar(
            title = title,
            modifier = modifier.clip(PillShape).background(fill).border(Sizes.hairline, scheme.outlineVariant, PillShape),
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = Sizes.topBarHeight,
            windowInsets = WindowInsets(0),
            scrollBehavior = scroll?.behavior,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
        )
    }
}

private const val SCROLLED_PX = 1f
