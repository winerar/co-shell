package app.coshell.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import app.coshell.core.designsystem.tokens.Motion
import kotlin.math.hypot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Theme / palette switch (design-refactor Step 4): the new look opens as a circle from the tapped switch.
// The old frame is captured once into a bitmap right before the change and drawn over the new one, clipped
// to the outside of a growing circle. One capture per switch; nothing is recorded in normal frames.

/**
 * Runs theme changes with a circular reveal. Get it with [LocalThemeReveal] inside [ThemeRevealHost];
 * outside a host (previews) [change] just applies the change.
 */
@Stable
class ThemeRevealState internal constructor(
    private val scope: CoroutineScope?,
    private val layer: GraphicsLayer?,
) {
    internal var capturing by mutableStateOf(false)
    internal var snapshot by mutableStateOf<ImageBitmap?>(null)
    internal var center = Offset.Unspecified
    internal val progress = Animatable(1f)

    /** Captures the current frame, then [apply]s the change (e.g. saves the new palette) and reveals it. */
    fun change(apply: () -> Unit) {
        val origin = PressOrigin.take()?.position
        if (scope == null || layer == null || snapshot != null || capturing) return apply()
        scope.launch {
            capturing = true
            // One frame to record the old look into the layer, then take it as a bitmap.
            withFrameNanos {}
            withFrameNanos {}
            val image = runCatching { layer.toImageBitmap() }.getOrNull()
            capturing = false
            if (image == null) return@launch apply()
            center = origin ?: Offset.Unspecified
            progress.snapTo(0f)
            snapshot = image
            apply()
            progress.animateTo(1f, Motion.spatialSlow())
            snapshot = null
        }
    }
}

/** The app's reveal; a no-op one (instant change) outside [ThemeRevealHost]. */
val LocalThemeReveal = staticCompositionLocalOf { ThemeRevealState(null, null) }

/** Wraps the whole app, above the theme: what [LocalThemeReveal] changes, it reveals in a circle. */
@Composable
fun ThemeRevealHost(content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val layer = rememberGraphicsLayer()
    val state = remember(scope, layer) { ThemeRevealState(scope, layer) }
    Box(
        Modifier.drawWithContent {
            if (state.capturing) {
                layer.record { this@drawWithContent.drawContent() }
                drawLayer(layer)
            } else {
                drawContent()
            }
            val old = state.snapshot ?: return@drawWithContent
            val center = state.center.takeIf { it.isSpecified } ?: Offset(size.width / 2f, size.height)
            val radius = revealRadius(center, size) * state.progress.value
            val hole = Path().apply { addOval(Rect(center, radius)) }
            clipPath(hole, ClipOp.Difference) { drawImage(old) }
        },
    ) {
        CompositionLocalProvider(LocalThemeReveal provides state, content = content)
    }
}

/** Distance from [center] to the farthest corner of [size]: the circle that covers the whole screen. Pure. */
internal fun revealRadius(center: Offset, size: Size): Float = maxOf(
    hypot(center.x, center.y),
    hypot(size.width - center.x, center.y),
    hypot(center.x, size.height - center.y),
    hypot(size.width - center.x, size.height - center.y),
)
