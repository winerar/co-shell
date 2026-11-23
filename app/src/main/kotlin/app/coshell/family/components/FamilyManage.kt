package app.coshell.family.components

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.model.Member
import app.coshell.ui.sheet.ModalSheet
import app.coshell.core.designsystem.R as DesignR

/** What the person is asked to confirm on «Семья»: leaving, removing someone, deleting the family or the account (#653). */
internal enum class FamilyConfirm { LEAVE, REMOVE, DELETE_FAMILY, DELETE_ACCOUNT }

/** The confirmation window of [kind]; [memberName] names the person being removed. */
@Composable
internal fun FamilyConfirmDialog(kind: FamilyConfirm, isOwner: Boolean, memberName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val (title, text, confirm) = when (kind) {
        FamilyConfirm.LEAVE -> Triple(
            stringResource(R.string.family_leave_title), stringResource(R.string.family_leave_text), R.string.family_leave_confirm,
        )
        FamilyConfirm.REMOVE -> Triple(
            stringResource(R.string.family_remove_title), stringResource(R.string.family_remove_text, memberName), R.string.family_remove_confirm,
        )
        FamilyConfirm.DELETE_FAMILY -> Triple(
            stringResource(R.string.family_delete_title), stringResource(R.string.family_delete_text), R.string.family_delete_confirm,
        )
        FamilyConfirm.DELETE_ACCOUNT -> Triple(
            stringResource(R.string.family_delete_account_title),
            stringResource(if (isOwner) R.string.family_delete_account_text_owner else R.string.family_delete_account_text_member),
            R.string.family_delete_account_confirm,
        )
    }
    CoShellAlertDialog(
        title = title,
        text = text,
        confirmLabel = stringResource(confirm),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        dismissLabel = stringResource(R.string.action_cancel),
        destructive = kind != FamilyConfirm.LEAVE,
    )
}

/** The owner's sheet for one member: the name, the address and «Исключить из семьи» (asks again before doing it). */
@Composable
internal fun MemberSheet(member: Member, onRemove: () -> Unit, onClosed: () -> Unit) {
    ModalSheet(title = member.name.ifBlank { member.email }, subtitle = member.email.takeIf { member.name.isNotBlank() }, onClosed = onClosed) {
        DangerRow(R.string.family_remove, R.string.family_remove_hint, enabled = true, onClick = onRemove)
    }
}

/** A row of a destructive action: title and icon in the error colour, one line of what happens. */
@Composable
internal fun DangerRow(@StringRes title: Int, @StringRes hint: Int, enabled: Boolean, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.error
    CoShellListRow(
        title = { Text(stringResource(title), color = color) },
        supporting = { Text(stringResource(hint)) },
        leading = { Icon(painterResource(DesignR.drawable.ic_delete), contentDescription = null, tint = color) },
        onClick = onClick,
        enabled = enabled,
    )
}
