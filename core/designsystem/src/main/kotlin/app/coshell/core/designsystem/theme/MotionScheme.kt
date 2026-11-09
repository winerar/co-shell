package app.coshell.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.material3.MotionScheme
import app.coshell.core.designsystem.tokens.Motion

/** Material components (switch, tabs, snackbar, date picker) move on the app's own springs — one tempo everywhere. */
internal object CoShellMotionScheme : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = Motion.spatial()
    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = Motion.spatialFast()
    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = Motion.spatialSlow()
    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = Motion.effects()
    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = Motion.effectsFast()
    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = Motion.effectsSlow()
}
