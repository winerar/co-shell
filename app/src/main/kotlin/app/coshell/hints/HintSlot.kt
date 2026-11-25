package app.coshell.hints

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCoachCard
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.R as DesignR

/**
 * The hint card on top of a tab's list (#513, owner poll 2026-11-25 «А», boards `Tip513Ops`, `Tip513HomeA`): gestures, warm moments and
 * notices from [HintHost.banner] lie inside the screen and never leave by themselves — there is time to read them. «Понятно» hides the card
 * (the next one in the queue takes its place); «Открыть» on a moment that names a screen goes there. Claimed off the tabs, a card waits for them.
 */
@Composable
fun HintSlot(modifier: Modifier = Modifier) {
    val host = LocalHintHost.current ?: return
    val banner by host.banner.collectAsStateWithLifecycle()
    // Keeps the text while the card folds away after it turned null.
    var last by remember { mutableStateOf(banner) }
    if (banner != null) last = banner
    val shown = last ?: return
    val target = shown.id?.target()
    CoShellExpandable(expanded = banner != null, modifier = modifier) {
        HintCard(
            text = stringResource(shown.text, *shown.args.toTypedArray()),
            onOk = host::dismiss,
            openLabel = target?.let { stringResource(R.string.hint_open) },
            onOpen = { target?.let(host::open) },
        )
    }
}

/** One look for every hint (words, gestures, moments): a lightbulb, the text in the normal ink, «Понятно» (and «Открыть»). */
@Composable
fun HintCard(text: String, onOk: () -> Unit, modifier: Modifier = Modifier, openLabel: String? = null, onOpen: () -> Unit = {}) =
    CoShellCoachCard(
        visible = true,
        text = text,
        actionLabel = stringResource(R.string.hint_ok),
        onAction = onOk,
        modifier = modifier,
        icon = DesignR.drawable.ic_lightbulb,
        secondaryLabel = openLabel,
        onSecondary = onOpen,
    )

/** True while a hint card waits on the tabs: lists with `spacedBy` add the slot item only then, so no empty gap stays. */
@Composable
fun hintSlotShown(): Boolean {
    val host = LocalHintHost.current ?: return false
    val banner by host.banner.collectAsStateWithLifecycle()
    return banner != null
}
