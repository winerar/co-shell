package app.coshell.update.data

import app.coshell.core.domain.update.AppRelease
import java.net.URI
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * `version.json` published next to the APK by CI (`scripts/make-version-json.sh`).
 * [apk] may be relative to the manifest's own URL, e.g. `co-shell-42.apk`.
 */
@Serializable
internal data class ReleaseManifest(
    val versionCode: Int,
    val versionName: String,
    val apk: String,
    val sha256: String,
    val size: Long,
    val notes: String = "",
)

private val json = Json { ignoreUnknownKeys = true }

/** Parses the manifest downloaded from [manifestUrl]; throws [IllegalArgumentException] on bad data. */
internal fun parseReleaseManifest(text: String, manifestUrl: String): AppRelease {
    val manifest = try {
        json.decodeFromString<ReleaseManifest>(text)
    } catch (e: Exception) {
        throw IllegalArgumentException("Bad version.json: ${e.message}", e)
    }
    val apkUrl = URI(manifestUrl).resolve(manifest.apk).toString()
    require(apkUrl.startsWith("https://")) { "APK must be served over https: $apkUrl" }
    require(manifest.sha256.matches(SHA256_HEX)) { "Bad sha256: ${manifest.sha256}" }
    require(manifest.size > 0) { "Bad size: ${manifest.size}" }
    return AppRelease(
        versionCode = manifest.versionCode,
        versionName = manifest.versionName,
        apkUrl = apkUrl,
        sha256 = manifest.sha256.lowercase(),
        sizeBytes = manifest.size,
        notes = manifest.notes.trim(),
    )
}

/** Same JSON shape, used to remember the pending release in preferences. */
internal fun AppRelease.toJson(): String = json.encodeToString(
    ReleaseManifest(versionCode, versionName, apkUrl, sha256, sizeBytes, notes),
)

internal fun releaseFromJson(text: String): AppRelease? = runCatching {
    val m = json.decodeFromString<ReleaseManifest>(text)
    AppRelease(m.versionCode, m.versionName, m.apk, m.sha256, m.size, m.notes)
}.getOrNull()

private val SHA256_HEX = Regex("[0-9a-fA-F]{64}")
