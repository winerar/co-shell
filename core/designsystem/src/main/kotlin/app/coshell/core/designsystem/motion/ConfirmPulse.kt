package app.coshell.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import app.coshell.core.designsystem.tokens.Motion
import kotlinx.coroutines.delay

/**
 * «Saved» signal for the bottom bar's «+» (design-refactor Step 4): an editor [fire]s it after a save, the
 * «+» morphs into a check mark, flashes warm and turns back. One per app, provided by the shell.
 */
@Stable
class ConfirmPulse {
    var count by mutableIntStateOf(0)
        private set

    fun fire() {
        count++
    }
}

val LocalAddConfirm = staticCompositionLocalOf { ConfirmPulse() }

/**
 * 0 → 1 → 0 each time [pulse] fires (not on first show): 1 = the check mark is shown. Holds the check for
 * [HOLD_MS] so it is seen, then springs back.
 */
@Composable
internal fun confirmProgress(pulse: ConfirmPulse?): Animatable<Float, *> {
    val progress = remember { Animatable(0f) }
    val count = pulse?.count ?: 0
    val seen = remember { intArrayOf(count) }
    LaunchedEffect(count) {
        if (count == seen[0]) return@LaunchedEffect
        seen[0] = count
        progress.animateTo(1f, Motion.spatial())
        delay(HOLD_MS)
        progress.animateTo(0f, Motion.spatial())
    }
    return progress
}

/**
 * The «+» icon turning into the check: [plus] turns a quarter and fades out as [check] turns in and grows.
 * [progress] — from [confirmProgress].
 */
@Composable
internal fun MorphIcon(progress: () -> Float, plus: @Composable () -> Unit, check: @Composable () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier.graphicsLayer {
                val p = progress()
                alpha = (1f - p).coerceIn(0f, 1f)
                rotationZ = p * QUARTER
                scaleX = 1f - p * SHRINK
                scaleY = scaleX
            },
        ) { plus() }
        Box(
            Modifier.graphicsLayer {
                val p = progress()
                alpha = p.coerceIn(0f, 1f)
                rotationZ = (p - 1f) * QUARTER
                scaleX = 1f - (1f - p) * SHRINK
                scaleY = scaleX
            },
        ) { check() }
    }
}

private const val HOLD_MS = 700L
private const val QUARTER = 90f
private const val SHRINK = 0.4f
