package app.coshell.privacy

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.designsystem.amount.LocalAmountsHidden
import app.coshell.privacy.lock.LockScreen

/** The privacy state for the screens («глаз» on «Обзоре», the face-down pill); `null` in previews and tests. */
val LocalPrivacyCenter = staticCompositionLocalOf<PrivacyCenter?> { null }

/**
 * Wraps the whole app (#145): waits until the privacy options are read (a few ms — the app never flashes before the
 * lock), hides the sums everywhere ([LocalAmountsHidden]), sets the window flags, listens for «экраном вниз» and
 * shows only the lock screen while locked. The app is taken out of composition then, not just covered: sheets and
 * dialogs are windows of their own and would stay above the lock (an open entry sheet, a shared receipt, a shake).
 * Its saved state is kept ([rememberSaveableStateHolder]), so after the PIN the same screen and sheet come back.
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
        Box {
            if (covered) lockScreen(o.biometric) else saved.SaveableStateProvider(APP_KEY) { content() }
        }
    }
}

private const val APP_KEY = "app"
