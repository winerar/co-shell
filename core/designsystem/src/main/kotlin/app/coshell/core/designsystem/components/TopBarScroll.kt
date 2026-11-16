package app.coshell.core.designsystem.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

/** Scroll link between a screen's list and its [CoShellTopBar]; hides Material's experimental type from screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class TabScroll internal constructor(private val behavior: TopAppBarScrollBehavior) {
    /**
     * True once the list has moved under the header: the header shrinks its title and draws a line. Read from the
     * scrolled distance, not `overlappedFraction`: that one needs a bar bound to the behaviour.
     */
    val scrolled: Boolean get() = behavior.state.contentOffset < -SCROLLED_PX

    internal val connection get() = behavior.nestedScrollConnection
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberTabScrollBehavior(): TabScroll {
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()
    return remember(behavior) { TabScroll(behavior) }
}

/** Lets the list's scroll reach the header; put it on the screen's Scaffold. */
fun Modifier.tabScroll(scroll: TabScroll): Modifier = nestedScroll(scroll.connection)

/** Less than a pixel of scroll is still «at the top». */
private const val SCROLLED_PX = 1f
