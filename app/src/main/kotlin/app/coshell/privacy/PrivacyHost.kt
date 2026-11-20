package app.coshell.privacy

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.designsystem.amount.LocalAmountsHidden
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.privacy.lock.LockScreen

/** The privacy state for the screens («глаз» on «Обзоре», the face-down pill); `null` in previews and tests. */
val LocalPrivacyCenter = staticCompositionLocalOf<PrivacyCenter?> { null }

/**
 * Wraps the whole app (#145): waits until the privacy options are read (a few ms — the app never flashes before the
 * lock), hides the sums everywhere ([LocalAmountsHidden]), sets the window flags, listens for «экраном вниз» and
 * shows only the lock screen while locked. The app is taken out of composition then, not just covered: sheets and
 * dialogs are windows of their own and would stay above the lock (an open entry sheet, a shared receipt, a shake).
 * Its saved state is kept ([rememberSaveableStateHolder]), so after the PIN the same screen and sheet come back.
 * The lock appears at once (nothing may peek through), but leaves softly: after the PIN it fades and lifts above
 * the app, which grows back from [APP_FROM] — like a screen closing (`Motion.effectsSlow`, `motion.md`).
 */
@Composable
fun PrivacyHost(
    center: PrivacyCenter,
    lockScreen: @Composable (biometric: Boolean) -> Unit = { LockScreen(biometric = it) },
    content: @Composable () -> Unit,
) {
    val options by center.options.collectAsStateWithLifecycle()
    val locked by center.locked.collectAsStateWithLifecycle()
    val hidden by center.hidden.collectAsStateWithLifecycle()
    val o = options ?: return
    LocalActivity.current?.let { WindowPrivacyEffect(it, o) }
    FaceDownEffect(enabled = o.hideFaceDown && !locked, onFaceDown = center::onFaceDown)
    CompositionLocalProvider(LocalPrivacyCenter provides center, LocalAmountsHidden provides hidden) {
        val covered = locked && o.pinEnabled
        val saved = rememberSaveableStateHolder()
        // 0 — the lock is shown, 1 — it is gone.
        val leaving = remember { Animatable(if (covered) 0f else 1f) }
        val lockGone by remember { derivedStateOf { leaving.value >= 1f } }
        LaunchedEffect(covered) { if (covered) leaving.snapTo(0f) else leaving.animateTo(1f, Motion.effectsSlow()) }
        Box {
            if (!covered) {
                Box(
                    Modifier.graphicsLayer {
                        val scale = APP_FROM + (1f - APP_FROM) * leaving.value
                        scaleX = scale
                        scaleY = scale
                    },
                ) { saved.SaveableStateProvider(APP_KEY) { content() } }
            }
            if (covered || !lockGone) {
                Box(
                    Modifier.graphicsLayer {
                        val gone = if (covered) 0f else leaving.value
                        alpha = 1f - gone
                        scaleX = 1f + LOCK_LIFT * gone
                        scaleY = 1f + LOCK_LIFT * gone
                    },
                ) { lockScreen(o.biometric) }
            }
        }
    }
}

private const val APP_KEY = "app"

/** The app's scale when the lock starts leaving (as `navigation/ScreenTransitions`: the screen behind steps back to 0.94). */
private const val APP_FROM = 0.94f

/** How much the leaving lock grows while it fades. */
private const val LOCK_LIFT = 0.06f
