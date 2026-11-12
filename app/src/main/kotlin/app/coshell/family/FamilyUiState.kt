package app.coshell.family

import app.coshell.core.data.family.FamilyState
import app.coshell.core.data.sync.SyncIssue
import app.coshell.core.data.sync.SyncIssueKind

/** «Семья и синхронизация» screen state. */
data class FamilyUiState(
    val family: FamilyState = FamilyState.SignedOut,
    val invites: List<String> = emptyList(),
    /** Local edits not yet sent to the family. */
    val pending: Int = 0,
    /** «сегодня 12:03» / «9 ноября 12:03», `null` — never synced. */
    val lastSync: String? = null,
    val isBusy: Boolean = false,
    /** Joining with own operations on the phone: ask whether to keep them. */
    val askJoinMode: Boolean = false,
    val message: FamilyMessage? = null,
)

/** Result of the last action, shown as a card until the next one. */
sealed interface FamilyMessage {
    val isError: Boolean get() = true

    data object SignInFailed : FamilyMessage
    data object FamilyExists : FamilyMessage
    /** An action failed; [kind] says why, as on the sync banner of «Обзора» (#124); [code] — server's answer. */
    data class ActionFailed(val kind: SyncIssueKind, val code: String? = null) : FamilyMessage
    data object InvalidEmail : FamilyMessage
    data class InviteSent(val email: String) : FamilyMessage { override val isError get() = false }
    data class Synced(val received: Int, val sent: Int) : FamilyMessage { override val isError get() = false }

    companion object {
        /** The message for a failed action: network, sign-in, access, quota or server (#124). */
        fun failed(error: Throwable): ActionFailed = SyncIssue.classify(error).let { (kind, code) -> ActionFailed(kind, code) }
    }
}
