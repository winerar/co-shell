package app.coshell.family.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.components.CoShellDialog
import app.coshell.core.designsystem.components.CoShellTextField

/** Asks for the Google email of the person to invite. */
@Composable
internal fun InviteDialog(onInvite: (String) -> Unit, onDismiss: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    CoShellDialog(
        title = stringResource(R.string.family_invite),
        confirmLabel = stringResource(R.string.family_invite_send),
        onConfirm = { onInvite(email) },
        onDismiss = onDismiss,
        dismissLabel = stringResource(R.string.action_cancel),
        confirmEnabled = email.isNotBlank(),
    ) {
        Text(stringResource(R.string.family_invite_dialog_text))
        CoShellTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.family_invite_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
    }
}

/** Joining with own operations on this phone: replace them with the family's or add them to it. */
@Composable
internal fun JoinModeDialog(onChoose: (keepLocalData: Boolean) -> Unit, onDismiss: () -> Unit) {
    CoShellAlertDialog(
        title = stringResource(R.string.family_join_mode_title),
        text = stringResource(R.string.family_join_mode_text),
        confirmLabel = stringResource(R.string.family_join_replace),
        onConfirm = { onChoose(false) },
        onDismiss = onDismiss,
        dismissLabel = stringResource(R.string.family_join_keep),
        onDismissClick = { onChoose(true) },
    )
}
