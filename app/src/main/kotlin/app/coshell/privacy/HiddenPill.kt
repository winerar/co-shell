package app.coshell.privacy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellNoticePill
import kotlinx.coroutines.delay
import app.coshell.core.designsystem.R as DesignR

/**
 * After laying the phone screen down (#145, board `Priv145Flipped`): «Суммы скрыты. Нажмите «глаз», чтобы показать.»
 * over the bottom bar; goes away by itself after a while, on «×» or when the sums are shown.
 */
@Composable
fun HiddenPill(visible: Boolean, modifier: Modifier = Modifier) {
    val center = LocalPrivacyCenter.current ?: return
    val notice by center.faceDownNotice.collectAsStateWithLifecycle()
    LaunchedEffect(notice) {
        if (notice) {
            delay(SHOWN_MILLIS)
            center.dismissFaceDownNotice()
        }
    }
    CoShellNoticePill(
        visible = visible && notice,
        icon = DesignR.drawable.ic_visibility_off,
        text = stringResource(R.string.amounts_hidden_pill),
        onClick = center::dismissFaceDownNotice,
        dismissLabel = stringResource(R.string.amounts_hidden_dismiss),
        onDismiss = center::dismissFaceDownNotice,
        modifier = modifier,
    )
}

private const val SHOWN_MILLIS = 8_000L
