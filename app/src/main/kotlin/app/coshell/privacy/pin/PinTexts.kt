package app.coshell.privacy.pin

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R

/** [PinFlowState] → what [PinLayout] shows: the title of the step and the line under it. */
@Composable
fun PinFlowState.toUi(): PinUi {
    val title = stringResource(
        when (step) {
            PinStep.ENTER -> R.string.lock_enter
            PinStep.CURRENT -> R.string.pin_current
            PinStep.CREATE -> R.string.pin_create
            PinStep.REPEAT -> R.string.pin_repeat
        },
    )
    val message = when (note) {
        PinNote.WRONG -> stringResource(R.string.lock_wrong)
        PinNote.WAIT -> if (waitSeconds > 0) stringResource(R.string.lock_wait, waitSeconds) else stringResource(R.string.lock_wrong)
        PinNote.MISMATCH -> stringResource(R.string.pin_mismatch)
        PinNote.NONE -> when (step) {
            PinStep.CREATE -> stringResource(R.string.pin_create_hint)
            PinStep.REPEAT -> stringResource(R.string.pin_repeat_hint)
            else -> ""
        }
    }
    return PinUi(title, message, error, errorKey, entry.digits.length, enabled)
}
