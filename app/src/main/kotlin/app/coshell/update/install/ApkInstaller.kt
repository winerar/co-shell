package app.coshell.update.install

import java.io.File

/** Installs a downloaded APK over the running app. */
interface ApkInstaller {
    /** Whether the user allowed this app to install apps ("unknown sources" for co-shell). */
    fun canInstall(): Boolean

    /** Opens the system screen where that permission is granted. */
    fun openInstallPermissionSettings()

    /** Starts the install; the result arrives through [InstallEvents]. [packageName] — the package inside [file] when it is not this app (the AI add-on, #587). */
    suspend fun install(file: File, packageName: String? = null)

    /**
     * Opens [file] in the Android installer, the way a browser does (#217): the fallback when the
     * [install] dialog never shows (Xiaomi). Android reports nothing back; on success the app restarts.
     */
    fun installManually(file: File)
}
