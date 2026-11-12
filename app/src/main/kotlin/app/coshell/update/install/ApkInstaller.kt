package app.coshell.update.install

import java.io.File

/** Installs a downloaded APK over the running app. */
interface ApkInstaller {
    /** Whether the user allowed this app to install apps ("unknown sources" for co-shell). */
    fun canInstall(): Boolean

    /** Opens the system screen where that permission is granted. */
    fun openInstallPermissionSettings()

    /** Starts the install; the result arrives through [InstallEvents]. */
    suspend fun install(file: File)

    /**
     * Opens [file] in the Android installer, the way a browser does (#217): the fallback when the
     * [install] dialog never shows (Xiaomi). Android reports nothing back; on success the app restarts.
     */
    fun installManually(file: File)
}
