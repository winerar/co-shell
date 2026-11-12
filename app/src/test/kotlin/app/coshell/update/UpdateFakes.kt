package app.coshell.update

import app.coshell.core.domain.update.AppRelease
import app.coshell.core.domain.update.ReleaseSource
import app.coshell.core.domain.update.UpdateStateStore
import app.coshell.update.data.ApkDownloader
import app.coshell.update.install.ApkInstaller
import app.coshell.update.log.UpdateJournal
import java.io.File
import kotlin.time.Instant

fun release(code: Int, notes: String = "Новое") =
    AppRelease(code, "0.1.$code", "https://x.web.app/co-shell-$code.apk", "ab".repeat(32), 2_000_000, notes)

class FakeReleaseSource(var release: AppRelease? = null, var error: Exception? = null) : ReleaseSource {
    override suspend fun latestRelease(): AppRelease? {
        error?.let { throw it }
        return release
    }
}

class FakeUpdateStateStore : UpdateStateStore {
    var lastCheck: Instant? = null
    var pending: AppRelease? = null
    var known: AppRelease? = null
    override suspend fun lastCheckAt() = lastCheck
    override suspend fun setLastCheckAt(at: Instant) { lastCheck = at }
    override suspend fun pendingRelease() = pending
    override suspend fun setPendingRelease(release: AppRelease?) { pending = release }
    override suspend fun knownRelease() = known
    override suspend fun setKnownRelease(release: AppRelease?) { known = release }
}

class FakeApkDownloader : ApkDownloader {
    var error: Exception? = null
    val downloaded = mutableListOf<AppRelease>()
    override suspend fun download(release: AppRelease, onProgress: (Float) -> Unit): File {
        error?.let { throw it }
        onProgress(0.5f)
        onProgress(1f)
        downloaded += release
        return File("co-shell-${release.versionCode}.apk")
    }
}

class FakeApkInstaller : ApkInstaller {
    var allowed = true
    var settingsOpened = 0
    val installed = mutableListOf<File>()
    override fun canInstall() = allowed
    override fun openInstallPermissionSettings() { settingsOpened++ }
    override suspend fun install(file: File) { installed += file }
    val manual = mutableListOf<File>()
    override fun installManually(file: File) { manual += file }
}

class FakeUpdateJournal : UpdateJournal {
    val events = mutableListOf<String>()
    override fun add(event: String) { events += event }
    override fun lines() = events.toList()
    override var preferManualInstall = false
}
