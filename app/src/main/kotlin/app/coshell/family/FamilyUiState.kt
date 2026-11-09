package app.coshell.family

import app.coshell.core.data.family.FamilyState

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
    data object NetworkError : FamilyMessage
    data object InvalidEmail : FamilyMessage
    data class InviteSent(val email: String) : FamilyMessage { override val isError get() = false }
    data class Synced(val received: Int, val sent: Int) : FamilyMessage { override val isError get() = false }
}
