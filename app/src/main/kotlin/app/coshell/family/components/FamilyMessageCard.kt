package app.coshell.family.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.family.FamilyMessage

/** Result of the last action on the family screen. */
@Composable
internal fun FamilyMessageCard(message: FamilyMessage, modifier: Modifier = Modifier) {
    val text = when (message) {
        FamilyMessage.SignInFailed -> stringResource(R.string.family_msg_sign_in_failed)
        FamilyMessage.FamilyExists -> stringResource(R.string.family_msg_exists)
        FamilyMessage.NetworkError -> stringResource(R.string.family_msg_network)
        FamilyMessage.InvalidEmail -> stringResource(R.string.family_msg_invalid_email)
        is FamilyMessage.InviteSent -> stringResource(R.string.family_msg_invite_sent, message.email)
        is FamilyMessage.Synced -> stringResource(R.string.family_msg_synced, message.received, message.sent)
    }
    CoShellMessage(text, message.isError, modifier)
}
