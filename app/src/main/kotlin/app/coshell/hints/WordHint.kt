package app.coshell.hints

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.domain.hints.HintId
import kotlinx.coroutines.delay

/** Null in previews and tests: no hint is ever shown there. */
val LocalHintHost = compositionLocalOf<HintHost?> { null }

/**
 * A word hint (П1): a card under the header (owner poll 2026-11-25 «А», board `Tip513Accounts`) — lightbulb, text, «Понятно».
 * Asks [LocalHintHost] once per screen visit; once claimed it stays until «Понятно». [emptyScreen] — the screen has no data yet:
 * the hint shows even if another one was shown today (owner 2026-11-25). [id] must be a `HintKind.WORD` hint with its text in `strings_hints.xml`.
 */
@Composable
fun WordHint(id: HintId, @StringRes text: Int, modifier: Modifier = Modifier, emptyScreen: Boolean = false) {
    val host = LocalHintHost.current ?: return
    var visible by rememberSaveable(id.key) { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (visible) return@LaunchedEffect
        delay(SHOW_DELAY_MS)
        visible = host.claim(id, emptyScreen)
    }
    CoShellExpandable(expanded = visible, modifier = modifier) {
        HintCard(stringResource(text), onOk = { visible = false })
    }
}
