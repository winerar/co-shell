package app.coshell.core.data.auth

import kotlinx.serialization.Serializable

/** Signed-in Firebase user. Tokens are secrets: stored only in app-private storage. */
@Serializable
data class AuthSession(
    val uid: String,
    val email: String,
    val name: String,
    val idToken: String,
    val refreshToken: String,
    /** Epoch ms when [idToken] stops being valid (Firebase issues 1-hour tokens). */
    val expiresAtMs: Long,
)

/** Keeps the session between app starts (`app`: private SharedPreferences). */
interface SessionStore {
    fun load(): AuthSession?
    fun save(session: AuthSession?)
}
