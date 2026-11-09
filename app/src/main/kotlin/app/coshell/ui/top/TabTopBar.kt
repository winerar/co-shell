package app.coshell.ui.top

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

/** Scroll link between a tab's list and its [TabTopBar]; hides Material's experimental type from screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class TabScroll internal constructor(internal val behavior: TopAppBarScrollBehavior)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberTabScrollBehavior(): TabScroll {
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    return remember(behavior) { TabScroll(behavior) }
}

/** Lets the list's scroll collapse the [TabTopBar]; put it on the screen's Scaffold. */
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.tabScroll(scroll: TabScroll): Modifier = nestedScroll(scroll.behavior.nestedScrollConnection)

/**
 * Header of a tab screen (Обзор, Операции, Счета, Ещё): a large title that pushes content down
 * towards the thumb and collapses into a thin bar while the list scrolls (design-questions §5).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabTopBar(title: String, scroll: TabScroll, actions: @Composable RowScope.() -> Unit = {}) {
    LargeTopAppBar(
        title = { Text(title) },
        actions = actions,
        scrollBehavior = scroll.behavior,
    )
}
