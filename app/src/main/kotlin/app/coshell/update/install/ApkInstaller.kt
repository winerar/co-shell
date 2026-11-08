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
}
