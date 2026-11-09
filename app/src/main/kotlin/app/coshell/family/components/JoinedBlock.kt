package app.coshell.family.components

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.data.family.FamilyState
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.family.FamilyActions
import app.coshell.family.FamilyUiState
import app.coshell.ui.list.ActionRow
import app.coshell.core.designsystem.R as DesignR

/** In the family: sync status, members, invitations, sign out. */
@Composable
internal fun JoinedBlock(joined: FamilyState.Joined, state: FamilyUiState, actions: FamilyActions) {
    var inviting by rememberSaveable { mutableStateOf(false) }

    val status = when {
        state.lastSync == null -> stringResource(R.string.family_never_synced)
        state.pending > 0 -> stringResource(R.string.family_synced_pending, state.lastSync, state.pending)
        else -> stringResource(R.string.family_synced_at, state.lastSync)
    }
    ActionRow(DesignR.drawable.ic_sync, stringResource(R.string.family_sync_now), actions.onSyncNow, subtitle = status, enabled = !state.isBusy)

    FamilySectionTitle(stringResource(R.string.family_members))
    joined.family.members.forEach { m ->
        val tags = listOfNotNull(
            stringResource(R.string.family_you).takeIf { m.uid == joined.me.uid },
            stringResource(R.string.family_owner).takeIf { m.uid == joined.family.ownerUid },
        )
        ListItem(
            headlineContent = { Text(m.name.ifBlank { m.email }) },
            supportingContent = { Text((listOf(m.email) + tags).joinToString(" · ")) },
        )
    }
    state.invites.forEach { email ->
        ListItem(
            headlineContent = { Text(email) },
            supportingContent = { Text(stringResource(R.string.family_invite_pending)) },
            trailingContent = {
                CoShellIconButton(onClick = { actions.onCancelInvite(email) }, enabled = !state.isBusy) {
                    Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.family_invite_cancel))
                }
            },
        )
    }
    ActionRow(
        icon = DesignR.drawable.ic_person_add,
        title = stringResource(R.string.family_invite),
        subtitle = stringResource(R.string.family_invite_hint),
        onClick = { inviting = true },
        enabled = !state.isBusy,
    )
    SignOutRow(state.isBusy, actions.onSignOut)

    if (inviting) {
        InviteDialog(
            onInvite = { inviting = false; actions.onInvite(it) },
            onDismiss = { inviting = false },
        )
    }
}
