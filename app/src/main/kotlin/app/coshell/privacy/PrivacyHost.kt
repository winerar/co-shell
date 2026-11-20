package app.coshell.privacy

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.designsystem.amount.LocalAmountsHidden
import app.coshell.privacy.lock.LockScreen

/** The privacy state for the screens («глаз» on «Обзоре», the face-down pill); `null` in previews and tests. */
val LocalPrivacyCenter = staticCompositionLocalOf<PrivacyCenter?> { null }

/**
 * Wraps the whole app (#145): waits until the privacy options are read (a few ms — the app never flashes before the
 * lock), hides the sums everywhere ([LocalAmountsHidden]), sets the window flags, listens for «экраном вниз» and
 * covers everything with the lock screen while locked. The app under it stays composed (the open screen survives), so unlocking is instant.
 */
@Composable
fun PrivacyHost(center: PrivacyCenter, content: @Composable () -> Unit) {
    val options by center.options.collectAsStateWithLifecycle()
    val locked by center.locked.collectAsStateWithLifecycle()
    val hidden by center.hidden.collectAsStateWithLifecycle()
    val o = options ?: return
    LocalActivity.current?.let { WindowPrivacyEffect(it, o) }
    FaceDownEffect(enabled = o.hideFaceDown && !locked, onFaceDown = center::onFaceDown)
    CompositionLocalProvider(LocalPrivacyCenter provides center, LocalAmountsHidden provides hidden) {
        val covered = locked && o.pinEnabled
        Box {
            // Under the lock the app is out of TalkBack's reach (and the lock screen takes every touch).
            Box(if (covered) Modifier.clearAndSetSemantics {} else Modifier) { content() }
            if (covered) LockScreen(biometric = o.biometric)
        }
    }
}
