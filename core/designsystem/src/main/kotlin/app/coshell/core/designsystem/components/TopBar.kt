package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
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
 * Always solid — the colour of the screen, or the raised fill when [raised] (the selection bar, so the mode is seen at
 * a glance); never tinted or glass. The line under it is the screens' `scrollHairline`.
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
    val fill = if (raised) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.background
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scroll?.behavior,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = fill, scrolledContainerColor = fill),
    )
}

private const val SCROLLED_PX = 1f
