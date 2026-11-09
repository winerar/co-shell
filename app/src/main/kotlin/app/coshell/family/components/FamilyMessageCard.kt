package app.coshell.family.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.tokens.Spacing
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
    val colors = if (message.isError) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    }
    Card(colors = colors, modifier = modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(Spacing.l))
    }
}
