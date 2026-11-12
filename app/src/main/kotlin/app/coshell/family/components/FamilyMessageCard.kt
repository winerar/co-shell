package app.coshell.family.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.data.sync.SyncIssueKind
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.family.FamilyMessage

/** Result of the last action on the family screen. */
@Composable
internal fun FamilyMessageCard(message: FamilyMessage, modifier: Modifier = Modifier) {
    val text = when (message) {
        FamilyMessage.SignInFailed -> stringResource(R.string.family_msg_sign_in_failed)
        FamilyMessage.FamilyExists -> stringResource(R.string.family_msg_exists)
        is FamilyMessage.ActionFailed -> when (message.kind) {
            SyncIssueKind.OFFLINE -> stringResource(R.string.family_msg_network)
            SyncIssueKind.SIGNED_OUT -> stringResource(R.string.family_msg_signed_out)
            SyncIssueKind.NO_ACCESS -> stringResource(R.string.family_msg_no_access, message.code.orEmpty())
            SyncIssueKind.QUOTA -> stringResource(R.string.family_msg_quota)
            SyncIssueKind.SERVER -> stringResource(R.string.family_msg_server, message.code.orEmpty())
        }
        FamilyMessage.InvalidEmail -> stringResource(R.string.family_msg_invalid_email)
        is FamilyMessage.InviteSent -> stringResource(R.string.family_msg_invite_sent, message.email)
        is FamilyMessage.Synced -> stringResource(R.string.family_msg_synced, message.received, message.sent)
    }
    CoShellMessage(text, message.isError, modifier)
}
