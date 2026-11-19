package app.coshell.hints

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.coshell.core.domain.hints.HintId
import kotlinx.coroutines.delay

/** Not in the first seconds after a screen opens (`hints.md` §«Когда показывать»). */
internal const val SHOW_DELAY_MS = 1_500L

/**
 * Offers a gesture hint (П2) from a screen: when [enabled] holds for a moment, [LocalHintHost] picks the first of
 * [texts] by priority that may show and puts it on the banner (`HintBannerHost`). Nothing in previews and tests.
 */
@Composable
fun CoachHint(texts: Map<HintId, Int>, enabled: Boolean = true) {
    val host = LocalHintHost.current ?: return
    LaunchedEffect(enabled, texts.keys) {
        if (!enabled) return@LaunchedEffect
        delay(SHOW_DELAY_MS)
        host.offer(texts)
    }
}
