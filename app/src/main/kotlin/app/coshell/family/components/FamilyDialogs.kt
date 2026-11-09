package app.coshell.family.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextButton

/** Asks for the Google email of the person to invite. */
@Composable
internal fun InviteDialog(onInvite: (String) -> Unit, onDismiss: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.family_invite)) },
        text = {
            Column {
                Text(stringResource(R.string.family_invite_dialog_text))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.family_invite_email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
            }
        },
        confirmButton = {
            CoShellTextButton(onClick = { onInvite(email) }, enabled = email.isNotBlank()) {
                Text(stringResource(R.string.family_invite_send))
            }
        },
        dismissButton = { CoShellTextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Joining with own operations on this phone: replace them with the family's or add them to it. */
@Composable
internal fun JoinModeDialog(onChoose: (keepLocalData: Boolean) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.family_join_mode_title)) },
        text = { Text(stringResource(R.string.family_join_mode_text)) },
        confirmButton = {
            CoShellTextButton(onClick = { onChoose(false) }) { Text(stringResource(R.string.family_join_replace)) }
        },
        dismissButton = {
            CoShellTextButton(onClick = { onChoose(true) }) { Text(stringResource(R.string.family_join_keep)) }
        },
    )
}
