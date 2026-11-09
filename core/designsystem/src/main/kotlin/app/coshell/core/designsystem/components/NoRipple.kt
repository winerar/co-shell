package app.coshell.core.designsystem.components

import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Turns off the Material ripple for one wrapped component: its feedback is the [tactile]
 * motion. Elsewhere (date picker, snackbar) the theme keeps a faint mint ripple.
 */
@Composable
internal fun NoRipple(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalRippleConfiguration provides null, content = content)
}
