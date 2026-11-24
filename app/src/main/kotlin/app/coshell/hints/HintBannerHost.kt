package app.coshell.hints

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellCoachCard
import app.coshell.core.designsystem.components.CoShellNoticePill
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.hints.HintKind
import kotlinx.coroutines.delay

/** A warm pill leaves by itself; a gesture card waits for «Понятно». */
private const val PILL_MS = 8_000L

/**
 * The hint banner over the bottom bar (boards `Hint513Gesture`, `Hint513Moment`, `Hint513Reset`): a gesture card or a
 * warm pill, whichever [HintHost.banner] holds. [visible] is false while something more important sits there
 * (the update pill) or off the tabs.
 */
@Composable
fun HintBannerHost(host: HintHost, visible: Boolean, modifier: Modifier = Modifier, onOpen: (HintTarget) -> Unit = {}) {
    val banner by host.banner.collectAsStateWithLifecycle()
    // Keeps the text while the banner slides away after it turned null.
    var last by remember { mutableStateOf(banner) }
    if (banner != null) last = banner
    val shown = last ?: return
    val text = stringResource(shown.text, *shown.args.toTypedArray())
    val gesture = shown.id?.kind == HintKind.GESTURE
    if (!gesture) LaunchedEffect(banner) {
        if (banner == null) return@LaunchedEffect
        delay(PILL_MS)
        host.dismiss()
    }
    if (gesture) {
        CoShellCoachCard(
            visible = visible && banner != null,
            text = text,
            actionLabel = stringResource(R.string.hint_ok),
            onAction = host::dismiss,
            modifier = modifier.padding(horizontal = Spacing.l),
        )
    } else {
        CoShellNoticePill(
            visible = visible && banner != null,
            icon = DesignR.drawable.ic_check,
            text = text,
            onClick = { shown.id?.target()?.let { host.dismiss(); onOpen(it) } ?: host.dismiss() },
            actionLabel = shown.id?.target()?.let { stringResource(R.string.hint_open) },
            dismissLabel = stringResource(R.string.hint_close),
            onDismiss = host::dismiss,
            modifier = modifier,
        )
    }
}
