package app.coshell.core.domain.update

/**
 * A published build of the app, as described by `version.json` on the update server.
 * [versionCode] grows with every build; [sha256] (lowercase hex) protects the download.
 */
data class AppRelease(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val sha256: String,
    val sizeBytes: Long,
    /** "What's new" text in Russian, shown before and after the update. */
    val notes: String,
)
