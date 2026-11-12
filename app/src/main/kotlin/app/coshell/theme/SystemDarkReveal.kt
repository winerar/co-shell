package app.coshell.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import app.coshell.core.designsystem.motion.LocalThemeReveal
import app.coshell.core.domain.settings.ThemeMode

// The phone switching dark mode while the app is open (#230): with «Как в системе» the new look opens as the
// same circle as a switch in settings, instead of a jump. The activity is not recreated on a uiMode change
// (AndroidManifest `configChanges`), so the change arrives here as a new configuration.

/** How the app follows the phone's dark mode turning [system] while it shows [shown]. Pure. */
internal fun systemDarkChange(shown: Boolean, system: Boolean, mode: ThemeMode?): SystemDarkChange = when {
    shown == system -> SystemDarkChange.NONE
    mode == ThemeMode.SYSTEM -> SystemDarkChange.REVEAL
    else -> SystemDarkChange.INSTANT // the app's own theme hides the change: nothing to animate
}

internal enum class SystemDarkChange { NONE, INSTANT, REVEAL }

/**
 * The phone's dark mode as the app shows it: a change is revealed in a circle (inside `ThemeRevealHost`)
 * when [mode] follows the system, and taken at once otherwise.
 */
@Composable
fun rememberRevealedSystemDark(mode: ThemeMode?): Boolean {
    val system = isSystemInDarkTheme()
    val latest by rememberUpdatedState(system)
    val reveal = LocalThemeReveal.current
    var shown by remember { mutableStateOf(system) }
    LaunchedEffect(system, mode) {
        when (systemDarkChange(shown, system, mode)) {
            SystemDarkChange.NONE -> Unit
            SystemDarkChange.INSTANT -> shown = system
            // The latest value, not this one: a quick switch back may land while the old frame is captured.
            SystemDarkChange.REVEAL -> reveal.change { shown = latest }
        }
    }
    return shown
}
