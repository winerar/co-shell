package app.coshell.core.domain.update

import kotlin.time.Instant

/** Where the latest release is announced (`version.json` in the public releases repo). */
interface ReleaseSource {
    /** `null` when updates are not configured in this build. Throws on network or format errors. */
    suspend fun latestRelease(): AppRelease?
}

/** Small persistent state of the update flow (SharedPreferences on Android). */
interface UpdateStateStore {
    suspend fun lastCheckAt(): Instant?
    suspend fun setLastCheckAt(at: Instant)

    /** Release being installed; remembered so its notes can be shown after the restart. */
    suspend fun pendingRelease(): AppRelease?
    suspend fun setPendingRelease(release: AppRelease?)
}

/** Version of the running app (from BuildConfig). */
data class InstalledVersion(val versionCode: Int, val versionName: String)
