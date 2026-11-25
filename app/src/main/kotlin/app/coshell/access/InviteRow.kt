package app.coshell.access

/**
 * The «Код приглашения» row in «Ещё → Настройки → Помощь» (#671, board `Inv671A`): who the code was issued to
 * ([label]) and whether the server closed it ([revoked], board `Inv671ARevoked`). Tap — the code screen with «Отмена».
 */
data class InviteRow(val label: String, val revoked: Boolean)

/** The row for this state; `null` — the build has no gate or there is no code yet (no row then). */
fun AccessState.inviteRow(): InviteRow? = when (this) {
    is AccessState.Granted -> InviteRow(label, revoked = false)
    is AccessState.Revoked -> InviteRow(label, revoked = true)
    AccessState.Loading, AccessState.Open, AccessState.Required -> null
}
