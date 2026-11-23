package app.coshell.ai.cloud

import app.coshell.core.data.remote.HttpClient
import app.coshell.core.data.remote.HttpRequest
import app.coshell.core.data.remote.IdTokenProvider
import app.coshell.core.domain.ai.LanguageModel
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put

/**
 * [LanguageModel] in the cloud (#635): sends the request to our Cloudflare Worker (`tools/ai-proxy`) with the Firebase ID token;
 * the Worker holds the Claude key. [available] only when this build knows the Worker address and the person is signed in.
 * Any failure (no network, daily limit, server error) is `null` — [app.coshell.core.domain.ai.RoutedLanguageModel] then asks the phone.
 * [onQuota] hears «left of limit» from every answer of the Worker (shown on the screen).
 */
class CloudLanguageModel(
    private val http: HttpClient,
    private val auth: IdTokenProvider,
    private val baseUrl: String,
    private val signedIn: () -> Boolean,
    private val onQuota: (left: Int, limit: Int) -> Unit = { _, _ -> },
) : LanguageModel {
    override val available: Boolean get() = baseUrl.isNotBlank() && signedIn()

    /** Free for the phone and quick: every note line goes here, with the accounts to choose from. */
    override val cheap: Boolean get() = true

    @Volatile
    override var limited: Boolean = false
        private set

    override suspend fun complete(system: String, prompt: String): String? {
        if (!available) return null
        return try {
            val body = buildJsonObject { put("system", system); put("prompt", prompt) }.toString()
            val response = http.send(HttpRequest("POST", baseUrl, body, mapOf("Authorization" to "Bearer ${auth.idToken()}")))
            val json = runCatching { Json.parseToJsonElement(response.body) as? JsonObject }.getOrNull()
            val left = (json?.get("left") as? JsonPrimitive)?.intOrNull
            val limit = (json?.get("limit") as? JsonPrimitive)?.intOrNull
            if (left != null && limit != null) onQuota(left, limit)
            limited = response.code == LIMIT_CODE
            if (!response.isSuccess) return null
            (json?.get("text") as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        const val LIMIT_CODE = 429
    }
}
