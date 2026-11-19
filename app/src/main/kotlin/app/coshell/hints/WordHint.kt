package app.coshell.hints

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.domain.hints.HintId
import androidx.compose.material3.Icon
import kotlinx.coroutines.delay

/** Null in previews and tests: no hint is ever shown there. */
val LocalHintHost = compositionLocalOf<HintHost?> { null }

/**
 * A word hint (П1): a line under the header with «✕». Asks [LocalHintHost] once per screen visit; once claimed it
 * stays until «✕». [id] must be a `HintKind.WORD` hint with its text in `strings_hints.xml`.
 */
@Composable
fun WordHint(id: HintId, @StringRes text: Int, modifier: Modifier = Modifier) {
    val host = LocalHintHost.current ?: return
    var visible by rememberSaveable(id.key) { mutableStateOf(false) }
    LaunchedEffect(id) {
        if (visible) return@LaunchedEffect
        delay(SHOW_DELAY_MS)
        visible = host.claim(id)
    }
    CoShellExpandable(expanded = visible, modifier = modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            CoShellHint(stringResource(text), Modifier.weight(1f))
            CoShellIconButton(onClick = { visible = false }) {
                Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.hint_close))
            }
        }
    }
}
