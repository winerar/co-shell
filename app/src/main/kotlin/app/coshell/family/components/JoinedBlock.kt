package app.coshell.family.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.data.family.FamilyState
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.ui.list.SectionTitle
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.family.FamilyActions
import app.coshell.family.FamilyUiState
import app.coshell.ui.list.ActionRow
import app.coshell.ui.hero.StatusCard
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.category.CategoryPalette
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
    // Family at a glance (mockup 13): who is in, when it last synced, «Синхронизировать сейчас» at hand.
    StatusCard(
        icon = DesignR.drawable.ic_group,
        title = stringResource(R.string.family_budget, joined.family.members.size),
        subtitle = status,
        tint = MaterialTheme.colorScheme.secondary,
        trailing = {
            CoShellIconButton(onClick = actions.onSyncNow, enabled = !state.isBusy) {
                Icon(painterResource(DesignR.drawable.ic_sync), stringResource(R.string.family_sync_now))
            }
        },
    )

    SectionTitle(stringResource(R.string.family_members))
    FamilyRows {
        joined.family.members.forEachIndexed { index, m ->
            row {
                val tags = listOfNotNull(
                    stringResource(R.string.family_you).takeIf { m.uid == joined.me.uid },
                    stringResource(R.string.family_owner).takeIf { m.uid == joined.family.ownerUid },
                )
                val name = m.name.ifBlank { m.email }
                CoShellListRow(
                    title = name,
                    subtitle = (listOf(m.email) + tags).joinToString(" · "),
                    leading = { AccountBadge(name, MemberColors[index % MemberColors.size]) },
                )
            }
        }
        state.invites.forEach { email ->
            row {
                CoShellListRow(
                    title = email,
                    subtitle = stringResource(R.string.family_invite_pending),
                    trailing = {
                        CoShellIconButton(onClick = { actions.onCancelInvite(email) }, enabled = !state.isBusy) {
                            Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.family_invite_cancel))
                        }
                    },
                )
            }
        }
        row {
            ActionRow(
                icon = DesignR.drawable.ic_person_add,
                title = stringResource(R.string.family_invite),
                subtitle = stringResource(R.string.family_invite_hint),
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

/** Avatar colours of members, in join order: violet, cyan, then warm ones (mockup 13). */
private val MemberColors: List<Long> = listOf(3, 6, 5, 2).map { CategoryPalette.colors[it] }
