package app.coshell.ui.top

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight

/** Scroll link between a tab's list and its [TabTopBar]; hides Material's experimental type from screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class TabScroll internal constructor(internal val behavior: TopAppBarScrollBehavior)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberTabScrollBehavior(): TabScroll {
    val behavior = TopAppBarDefaults.pinnedScrollBehavior()
    return remember(behavior) { TabScroll(behavior) }
}

/** Lets the list's scroll tint the [TabTopBar]; put it on the screen's Scaffold. */
@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.tabScroll(scroll: TabScroll): Modifier = nestedScroll(scroll.behavior.nestedScrollConnection)

/**
 * Header of a tab screen (Обзор, Операции, Счета, Ещё): one compact row with a bold title, as in the
 * mockup. The tall collapsing header left a third of the screen empty (owner, 2026-11-10); the content is
 * near the thumb anyway thanks to the bottom bar and the bottom-heavy layouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabTopBar(title: String, scroll: TabScroll, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
        actions = actions,
        scrollBehavior = scroll.behavior,
    )
}
