package app.coshell.family.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import app.coshell.core.model.Family
import app.coshell.ui.list.ActionRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.theme.secondaryInk
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
    var memberSheet by rememberSaveable { mutableStateOf<String?>(null) }
    var removeUid by rememberSaveable { mutableStateOf<String?>(null) }
    var confirm by rememberSaveable { mutableStateOf<FamilyConfirm?>(null) }
    val removalTarget = removeUid?.let { joined.family.member(it) }
    val isOwner = joined.family.isOwner(joined.me.uid)

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
                // The owner opens another member's sheet («Исключить из семьи»); nobody else has anything to do there.
                val manage = isOwner && !isMe
                CoShellListRow(
                    title = if (isMe) stringResource(R.string.family_you) else m.name.ifBlank { m.email },
                    // Me: only the role (my address is mine to know); others: the address they signed in with.
                    subtitle = if (isMe) owner ?: m.email else listOfNotNull(m.email, owner).joinToString(" · "),
                    leading = { MemberAvatar(m, index) },
                    trailing = if (manage) {
                        { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) }
                    } else null,
                    onClick = if (manage) ({ memberSheet = m.uid }) else null,
                    enabled = !state.isBusy,
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
            val full = joined.family.isFull
            CoShellListRow(
                title = { Text(stringResource(R.string.family_invite), color = if (full) Color.Unspecified else secondaryInk()) },
                supporting = {
                    Text(if (full) stringResource(R.string.family_invite_full, Family.MAX_MEMBERS) else stringResource(R.string.family_invite_hint))
                },
                leading = { InviteAvatar() },
                onClick = { inviting = true },
                enabled = !state.isBusy && !full,
            )
        }
    }
    SectionTitle(stringResource(R.string.family_account_section))
    FamilyRows {
        row {
            // The owner cannot leave: the family would be left without one — they delete it instead (#653).
            ActionRow(
                icon = DesignR.drawable.ic_logout,
                title = stringResource(R.string.family_leave),
                subtitle = stringResource(if (isOwner) R.string.family_leave_owner_hint else R.string.family_leave_hint),
                onClick = { confirm = FamilyConfirm.LEAVE },
                enabled = !state.isBusy && !isOwner,
            )
        }
        row { SignOutRow(state.isBusy, actions.onSignOut) }
    }
    SectionTitle(stringResource(R.string.family_danger_section))
    FamilyRows {
        if (isOwner) {
            row {
                DangerRow(R.string.family_delete, R.string.family_delete_hint, !state.isBusy) { confirm = FamilyConfirm.DELETE_FAMILY }
            }
        }
        row {
            DangerRow(R.string.family_delete_account, R.string.family_delete_account_hint, !state.isBusy) { confirm = FamilyConfirm.DELETE_ACCOUNT }
        }
    }

    if (inviting) {
        InviteDialog(
            onInvite = { inviting = false; actions.onInvite(it) },
            onDismiss = { inviting = false },
        )
    }
    val target = memberSheet?.let { uid -> joined.family.member(uid) }
    if (target != null) {
        MemberSheet(
            member = target,
            onRemove = { removeUid = target.uid; memberSheet = null; confirm = FamilyConfirm.REMOVE },
            onClosed = { memberSheet = null },
        )
    }
    confirm?.takeIf { it != FamilyConfirm.REMOVE || removalTarget != null }?.let { kind ->
        FamilyConfirmDialog(
            kind = kind,
            isOwner = isOwner,
            memberName = removalTarget?.let { it.name.ifBlank { it.email } }.orEmpty(),
            onConfirm = {
                confirm = null
                when (kind) {
                    FamilyConfirm.LEAVE -> actions.onLeave()
                    FamilyConfirm.DELETE_FAMILY -> actions.onDeleteFamily()
                    FamilyConfirm.DELETE_ACCOUNT -> actions.onDeleteAccount()
                    FamilyConfirm.REMOVE -> removalTarget?.let { actions.onRemoveMember(it.uid) }
                }
            },
            onDismiss = { confirm = null },
        )
    }
}
