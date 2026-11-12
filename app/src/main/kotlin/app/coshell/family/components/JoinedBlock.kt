package app.coshell.family.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.data.family.FamilyState
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.ui.list.SectionTitle
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.family.FamilyActions
import app.coshell.family.FamilyUiState
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
    val freshness = when {
        state.lastSync == null -> SyncFreshness.NEVER
        state.pending > 0 -> SyncFreshness.PENDING
        else -> SyncFreshness.SYNCED
    }
    FamilyHero(joined.family.members, status, freshness)
    SyncNowButton(enabled = !state.isBusy, onClick = actions.onSyncNow)

    SectionTitle(stringResource(R.string.family_members))
    FamilyRows {
        joined.family.members.forEachIndexed { index, m ->
            row {
                val isMe = m.uid == joined.me.uid
                val owner = stringResource(R.string.family_owner).takeIf { m.uid == joined.family.ownerUid }
                CoShellListRow(
                    title = if (isMe) stringResource(R.string.family_you) else m.name.ifBlank { m.email },
                    // Me: only the role (my address is mine to know); others: the address they signed in with.
                    subtitle = if (isMe) owner ?: m.email else listOfNotNull(m.email, owner).joinToString(" · "),
                    leading = { MemberAvatar(m, index) },
                )
            }
        }
        state.invites.forEach { email ->
            row {
                CoShellListRow(
                    title = email,
                    subtitle = stringResource(R.string.family_invite_pending),
                    leading = { InviteAvatar() },
                    trailing = {
                        CoShellIconButton(onClick = { actions.onCancelInvite(email) }, enabled = !state.isBusy) {
                            Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.family_invite_cancel))
                        }
                    },
                )
            }
        }
        row {
            CoShellListRow(
                title = { Text(stringResource(R.string.family_invite), color = accentInk()) },
                supporting = { Text(stringResource(R.string.family_invite_hint)) },
                leading = { InviteAvatar() },
                onClick = { inviting = true },
                enabled = !state.isBusy,
            )
        }
    }
    SectionTitle(stringResource(R.string.family_account_section))
    FamilyRows { row { SignOutRow(state.isBusy, actions.onSignOut) } }

    if (inviting) {
        InviteDialog(
            onInvite = { inviting = false; actions.onInvite(it) },
            onDismiss = { inviting = false },
        )
    }
}
