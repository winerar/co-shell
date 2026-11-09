package app.coshell.core.data.auth

import app.coshell.core.data.remote.FirestoreJson
import app.coshell.core.data.remote.HttpClient
import app.coshell.core.data.remote.HttpRequest
import app.coshell.core.data.remote.orThrow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject

/**
 * Firebase Auth over REST (no SDK): exchange a Google ID token for a Firebase session and refresh it.
 * https://firebase.google.com/docs/reference/rest/auth
 * [identityHost]/[tokenHost] point to the Auth emulator in tests.
 */
class FirebaseAuthApi(
    private val http: HttpClient,
    private val apiKey: String,
    private val identityHost: String = "https://identitytoolkit.googleapis.com",
    private val tokenHost: String = "https://securetoken.googleapis.com",
) {
    suspend fun signInWithGoogle(googleIdToken: String, nowMs: Long): AuthSession {
        val body = buildJsonObject {
            put("postBody", "id_token=$googleIdToken&providerId=google.com")
            put("requestUri", "http://localhost")
            put("returnSecureToken", true)
            put("returnIdpCredential", true)
        }
        val json = post("$identityHost/v1/accounts:signInWithIdp?key=$apiKey", body.toString())
        return AuthSession(
            uid = json.text("localId")!!,
            email = json.text("email").orEmpty(),
            name = json.text("displayName") ?: json.text("fullName") ?: json.text("email").orEmpty(),
            idToken = json.text("idToken")!!,
            refreshToken = json.text("refreshToken")!!,
            expiresAtMs = nowMs + (json.text("expiresIn")?.toLongOrNull() ?: 3600) * 1000,
        )
    }

    suspend fun refresh(session: AuthSession, nowMs: Long): AuthSession {
        val json = http.send(
            HttpRequest(
                method = "POST",
                url = "$tokenHost/v1/token?key=$apiKey",
                body = "grant_type=refresh_token&refresh_token=${session.refreshToken}",
                contentType = "application/x-www-form-urlencoded",
            ),
        ).orThrow().body.let { FirestoreJson.parseToJsonElement(it).jsonObject }
        return session.copy(
            idToken = json.text("id_token")!!,
            refreshToken = json.text("refresh_token") ?: session.refreshToken,
            expiresAtMs = nowMs + (json.text("expires_in")?.toLongOrNull() ?: 3600) * 1000,
        )
    }

    private suspend fun post(url: String, body: String): JsonObject =
        FirestoreJson.parseToJsonElement(http.send(HttpRequest("POST", url, body)).orThrow().body).jsonObject

    private fun JsonObject.text(key: String) = this[key]?.jsonPrimitive?.contentOrNull
}
