package app.coshell.core.designsystem.components

import android.os.Build
import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.view.WindowCompat
import app.coshell.core.designsystem.tokens.Spacing

// Phones with on-screen navigation buttons (three or two buttons at the bottom) instead of gestures. Their bar is a
// tappable band ~48 dp tall: Android draws a grey see-through scrim under it, and the app's lists show through.
// Here the band gets the screen's own background, and the floating capsules sit closer to it (they already clear it).

/** True when the bottom of the screen has navigation buttons, not a gesture handle: only buttons are a tappable element. */
@Composable
internal fun hasNavigationButtons(): Boolean = WindowInsets.tappableElement.getBottom(LocalDensity.current) > 0

/** Space under a floating capsule (bottom bar, action bar) above the system bar: the button band already stands off the edge. */
internal fun capsuleBottomGap(navigationButtons: Boolean): Dp = if (navigationButtons) Spacing.s else Spacing.l

/**
 * The band behind the navigation buttons in the screen's background colour, so list text never runs under the buttons
 * (the system scrim is switched off by [clearNavigationScrim]). Nothing with gesture navigation. Put it last in the root box.
 */
@Composable
fun BoxScope.CoShellNavButtonsBackdrop() {
    if (!hasNavigationButtons()) return
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .windowInsetsBottomHeight(WindowInsets.navigationBars)
            .background(MaterialTheme.colorScheme.background),
    )
}

/**
 * No grey scrim under the navigation buttons, and their icons in the theme's contrast ([dark] — light icons).
 * For the activity window and for a sheet's dialog window, which has its own bar settings.
 */
fun Window.clearNavigationScrim(dark: Boolean) {
    @Suppress("DEPRECATION")
    navigationBarColor = Color.Transparent.toArgb()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isNavigationBarContrastEnforced = false
    WindowCompat.getInsetsController(this, decorView).isAppearanceLightNavigationBars = !dark
}
