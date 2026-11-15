package app.coshell.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.coshell.ui.top.ScreenScrollHost

/**
 * A full-screen destination: [composable] wrapped in [ScreenScrollHost], so the screen's header knows when its
 * content has scrolled under it (#394 Б: the hairline) without each screen wiring that by hand.
 */
inline fun <reified T : Any> NavGraphBuilder.screen(
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T> { entry -> ScreenScrollHost { content(entry) } }
}
