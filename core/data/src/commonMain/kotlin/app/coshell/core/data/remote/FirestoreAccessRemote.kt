package app.coshell.core.data.remote

import app.coshell.core.domain.access.AccessRemote
import app.coshell.core.domain.access.CodeLookup
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/**
 * `access_codes/{code}` (#666): `label`, `revoked`, `maxDevices`, `devices`. No sign-in: the rules let anyone read one code by its
 * exact id and add an installation to `devices` (firebase/firestore.rules), so only the project's public Web API key is sent.
 * Blank [projectId] or [apiKey] (local builds) means there is no gate to ask.
 */
class FirestoreAccessRemote(
    private val http: HttpClient,
    private val projectId: String,
    private val apiKey: String,
    private val host: String = FirestoreClient.PRODUCTION_HOST,
) : AccessRemote {
    private val root get() = "projects/$projectId/databases/(default)/documents"

    override suspend fun lookup(code: String): CodeLookup {
        val response = http.send(HttpRequest("GET", "$host/v1/$root/access_codes/$code?key=$apiKey"))
        // A code that does not exist and one the rules hide look the same on purpose.
        if (response.code == 404 || response.code == 403) return CodeLookup.NotFound
        val fields = FsFields.of(FirestoreJson.parseToJsonElement(response.orThrow().body).jsonObject)
        val label = fields.string("label").orEmpty()
        return if (fields.bool("revoked") == true) {
            CodeLookup.Revoked(label)
        } else {
            CodeLookup.Valid(label, fields.strings("devices"), (fields.long("maxDevices") ?: 0L).toInt())
        }
    }

    override suspend fun addDevice(code: String, installationId: String): Boolean {
        val write = buildJsonObject {
            put("update", buildJsonObject { put("name", "$root/access_codes/$code") })
            put("updateMask", buildJsonObject { put("fieldPaths", buildJsonArray { }) })
            put("currentDocument", buildJsonObject { put("exists", true) })
            put("updateTransforms", buildJsonArray {
                add(buildJsonObject {
                    put("fieldPath", "devices")
                    put("appendMissingElements", buildJsonObject { put("values", buildJsonArray { add(fsString(installationId)) }) })
                })
            })
        }
        val body = JsonObject(mapOf("writes" to buildJsonArray { add(write) })).toString()
        val response = http.send(HttpRequest("POST", "$host/v1/$root:commit?key=$apiKey", body))
        if (response.code == 403) return false // the limit is reached, or the code was revoked meanwhile
        response.orThrow()
        return true
    }
}
