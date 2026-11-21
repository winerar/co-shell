package app.coshell.ai

import android.content.Context
import app.coshell.core.domain.update.AppRelease
import app.coshell.core.domain.update.ReleaseSource
import app.coshell.update.data.ApkDownloader
import app.coshell.update.install.ApkInstaller
import java.io.File

/** Getting the add-on's APK onto the phone (#587): where it is published, the download, the system install dialog. */
interface AddonPackage {
    /** The published add-on, `null` when there is none (or no internet). */
    suspend fun latest(): AppRelease?
    suspend fun download(release: AppRelease, onProgress: (Float) -> Unit): File
    suspend fun install(file: File)
}

internal class ReleaseAddonPackage(
    private val source: ReleaseSource,
    private val downloader: ApkDownloader,
    private val installer: ApkInstaller,
) : AddonPackage {
    override suspend fun latest(): AppRelease? = runCatching { source.latestRelease() }.getOrNull()
    override suspend fun download(release: AppRelease, onProgress: (Float) -> Unit): File = downloader.download(release, onProgress)
    override suspend fun install(file: File) = installer.install(file, AddonProtocol.PACKAGE)

    companion object {
        /**
         * The add-on lives under a fixed tag of the public releases repository, next to the app's own files:
         * `…/releases/latest/download` → `…/releases/download/ai-addon` (`version.json` + the APK, `ai-addon.yml`).
         */
        fun baseUrl(updateBaseUrl: String): String = updateBaseUrl.replace("/latest/download", "/download/ai-addon")
    }
}
