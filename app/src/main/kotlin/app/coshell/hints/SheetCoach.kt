package app.coshell.hints

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellCoachCard
import app.coshell.core.domain.hints.HintId
import kotlinx.coroutines.delay

/**
 * A hint card (П2) for a sheet, placed by [modifier] in the dimmed strip above it (owner poll 2026-11-19, Б; board
 * `Hint513EntryCoach`). Asks [LocalHintHost] once per opening for the first of [texts] by priority that may show; stays until
 * «Понятно». Nothing in previews and tests.
 */
@Composable
fun SheetCoach(texts: Map<HintId, Int>, modifier: Modifier = Modifier) {
    val host = LocalHintHost.current ?: return
    var shown by rememberSaveable { mutableStateOf<String?>(null) }
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shown != null) return@LaunchedEffect
        delay(SHOW_DELAY_MS)
        host.claimFirst(texts.keys)?.let {
            shown = it.key
            visible = true
        }
    }
    @StringRes val text = shown?.let(HintId::fromKey)?.let(texts::get) ?: texts.values.first()
    CoShellCoachCard(
        visible = visible,
        text = stringResource(text),
        actionLabel = stringResource(R.string.hint_ok),
        onAction = { visible = false },
        modifier = modifier,
        icon = DesignR.drawable.ic_lightbulb,
    )
}
