package app.coshell.core.designsystem.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import app.coshell.core.designsystem.tokens.Motion

/**
 * The same place shows other data (owner 2026-11-27: «в карточках отчётов переключение между данными — плавно»): another
 * period, month, day or «30 → 60 дней». The old words lift a little and fade, the new ones rise into place, the box takes
 * its new size on the spring — nothing jumps. For `AnimatedContent(transitionSpec = { dataSwap() })`; charts move their
 * marks themselves (`charts/ChartMorph.kt`).
 */
fun dataSwap(): ContentTransform = ContentTransform(
    targetContentEnter = fadeIn(Motion.effects()) + slideInVertically(Motion.Placement) { it / SHIFT },
    initialContentExit = fadeOut(Motion.effectsFast()) + slideOutVertically(Motion.Placement) { -it / SHIFT },
    sizeTransform = SizeTransform(clip = false) { _, _ -> Motion.Resize },
)

/** The words travel a quarter of their height: enough to read as «changed», not as «moved away». */
private const val SHIFT = 4
