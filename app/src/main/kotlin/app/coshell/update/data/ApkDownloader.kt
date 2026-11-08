package app.coshell.update.data

import android.content.Context
import app.coshell.core.domain.update.AppRelease
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Thrown when the downloaded file does not match the checksum from `version.json`. */
class ChecksumMismatchException : IOException("SHA-256 mismatch")

/** Downloads release APKs into the app cache and checks their SHA-256. */
interface ApkDownloader {
    /** Returns the verified APK; [onProgress] gets 0..1. Throws [IOException] on failure. */
    suspend fun download(release: AppRelease, onProgress: (Float) -> Unit): File
}

class CacheApkDownloader @Inject constructor(@ApplicationContext context: Context) : ApkDownloader {

    private val dir = File(context.cacheDir, "updates")

    override suspend fun download(release: AppRelease, onProgress: (Float) -> Unit): File =
        withContext(Dispatchers.IO) {
            dir.mkdirs()
            val target = File(dir, "co-shell-${release.versionCode}.apk")
            // A finished earlier download is reused; anything else in the folder is stale.
            if (target.exists() && target.sha256() == release.sha256) return@withContext target
            dir.listFiles()?.forEach { it.delete() }

            val partial = File(dir, target.name + ".part")
            val connection = URL(release.apkUrl).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("HTTP ${connection.responseCode}")
                }
                connection.inputStream.use { input ->
                    partial.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var done = 0L
                        while (true) {
                            ensureActive()
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            done += read
                            onProgress((done.toFloat() / release.sizeBytes).coerceAtMost(1f))
                        }
                    }
                }
            } finally {
                connection.disconnect()
            }
            if (partial.sha256() != release.sha256) {
                partial.delete()
                throw ChecksumMismatchException()
            }
            if (!partial.renameTo(target)) throw IOException("Cannot rename ${partial.name}")
            target
        }

    private companion object {
        const val TIMEOUT_MS = 30_000
    }
}
