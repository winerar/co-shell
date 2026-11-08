package app.coshell.core.domain.update

import kotlin.time.Clock
import kotlin.time.Instant

fun release(code: Int, notes: String = "Новое") = AppRelease(
    versionCode = code,
    versionName = "0.1.$code",
    apkUrl = "https://example.web.app/co-shell-$code.apk",
    sha256 = "ab".repeat(32),
    sizeBytes = 1_000,
    notes = notes,
)

class FakeReleaseSource(var release: AppRelease? = null, var error: Exception? = null) : ReleaseSource {
    var calls = 0
    override suspend fun latestRelease(): AppRelease? {
        calls++
        error?.let { throw it }
        return release
    }
}

class FakeUpdateStateStore : UpdateStateStore {
    var lastCheck: Instant? = null
    var pending: AppRelease? = null
    override suspend fun lastCheckAt() = lastCheck
    override suspend fun setLastCheckAt(at: Instant) { lastCheck = at }
    override suspend fun pendingRelease() = pending
    override suspend fun setPendingRelease(release: AppRelease?) { pending = release }
}

class FixedClock(var now: Instant) : Clock {
    override fun now() = now
}
