package app.coshell.family.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.family.FamilyActions
import app.coshell.ui.list.ActionRow
import app.coshell.core.designsystem.R as DesignR

// Blocks of the family screen before the phone is in the family.

@Composable
internal fun NotConfiguredBlock() = FamilyHint(stringResource(R.string.family_not_configured))

@Composable
internal fun SignedOutBlock(busy: Boolean, onSignIn: () -> Unit) {
    FamilyHint(stringResource(R.string.family_intro))
    ActionRow(
        icon = DesignR.drawable.ic_login,
        title = stringResource(R.string.family_sign_in),
        subtitle = stringResource(R.string.family_sign_in_hint),
        onClick = onSignIn,
        enabled = !busy,
    )
}

@Composable
internal fun CheckingBlock(email: String) = FamilyHint(stringResource(R.string.family_checking, email))

@Composable
internal fun NoAccessBlock(email: String, busy: Boolean, actions: FamilyActions) {
    FamilyHint(stringResource(R.string.family_no_access, email))
    ActionRow(DesignR.drawable.ic_refresh, stringResource(R.string.family_check_again), actions.onRetry, enabled = !busy)
    ActionRow(
        icon = DesignR.drawable.ic_group,
        title = stringResource(R.string.family_create),
        subtitle = stringResource(R.string.family_create_hint),
        onClick = actions.onCreateFamily,
        enabled = !busy,
    )
    SignOutRow(busy, actions.onSignOut)
}

@Composable
internal fun InvitedBlock(email: String, busy: Boolean, actions: FamilyActions) {
    FamilyHint(stringResource(R.string.family_invited, email))
    ActionRow(
        icon = DesignR.drawable.ic_group,
        title = stringResource(R.string.family_join),
        subtitle = stringResource(R.string.family_join_hint),
        onClick = actions.onJoin,
        enabled = !busy,
    )
    SignOutRow(busy, actions.onSignOut)
}

@Composable
internal fun OfflineBlock(email: String, busy: Boolean, actions: FamilyActions) {
    FamilyHint(stringResource(R.string.family_offline, email))
    ActionRow(DesignR.drawable.ic_refresh, stringResource(R.string.family_check_again), actions.onRetry, enabled = !busy)
    SignOutRow(busy, actions.onSignOut)
}

@Composable
internal fun SignOutRow(busy: Boolean, onSignOut: () -> Unit) = ActionRow(
    icon = DesignR.drawable.ic_logout,
    title = stringResource(R.string.family_sign_out),
    subtitle = stringResource(R.string.family_sign_out_hint),
    onClick = onSignOut,
    enabled = !busy,
)
