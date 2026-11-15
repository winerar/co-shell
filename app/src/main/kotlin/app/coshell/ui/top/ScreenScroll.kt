package app.coshell.ui.top

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * The scroll of the inner screen around it, for [ScreenTopBar]'s hairline (#394 Б). Null outside [ScreenScrollHost]
 * (previews, screenshot tests of a bare screen): the header then never draws the line.
 */
val LocalScreenScroll = staticCompositionLocalOf<TabScroll?> { null }

/**
 * Listens to whatever scrolls inside [content] (the screen's list goes up through its Scaffold to here) and tells
 * the [ScreenTopBar] below — so no screen wires its scroll by hand. One per navigation destination: each screen
 * starts unscrolled.
 */
@Composable
fun ScreenScrollHost(content: @Composable () -> Unit) {
    val scroll = rememberTabScrollBehavior()
    Box(Modifier.tabScroll(scroll)) {
        CompositionLocalProvider(LocalScreenScroll provides scroll, content = content)
    }
}
