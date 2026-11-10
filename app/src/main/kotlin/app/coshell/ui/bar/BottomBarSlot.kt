package app.coshell.ui.bar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Lets a tab screen put its own bar where the bottom navigation is — the «Дублировать · Удалить» of the
 * selection mode (19В). The app shell ([app.coshell.navigation.MainShell]) shows [content] instead of the tabs
 * and stops tab swipes while it is set.
 */
@Stable
class BottomBarSlot {
    /** Buttons for the capsule (`CoShellActionBar`); `null` — the tabs are shown. */
    var content: (@Composable RowScope.() -> Unit)? by mutableStateOf(null)
        internal set
}

val LocalBottomBarSlot = staticCompositionLocalOf { BottomBarSlot() }

/** While [active], the bottom navigation gives its place to the buttons in [content]; it comes back when this leaves. */
@Composable
fun ReplaceBottomBar(active: Boolean, content: @Composable RowScope.() -> Unit) {
    val slot = LocalBottomBarSlot.current
    val latest by rememberUpdatedState(content)
    DisposableEffect(active, slot) {
        val mine: @Composable RowScope.() -> Unit = { latest() }
        if (active) slot.content = mine
        onDispose { if (slot.content === mine) slot.content = null }
    }
}
