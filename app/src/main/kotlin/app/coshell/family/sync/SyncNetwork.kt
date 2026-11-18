package app.coshell.family.sync

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.work.Constraints
import androidx.work.NetworkType
import app.coshell.settings.PrefsScreenOptions

/**
 * «Данные и копия → Синхронизировать только по Wi-Fi»: the sync waits for an unmetered network. Read straight from the
 * preferences file because the workers start without the UI (no Hilt view model).
 */
object SyncNetwork {
    fun wifiOnly(context: Context): Boolean =
        context.getSharedPreferences(PrefsScreenOptions.FILE, Context.MODE_PRIVATE).getBoolean(PrefsScreenOptions.KEY_SYNC_WIFI, false)

    /** What WorkManager needs to run a sync job. */
    fun constraints(context: Context): Constraints =
        Constraints.Builder().setRequiredNetworkType(if (wifiOnly(context)) NetworkType.UNMETERED else NetworkType.CONNECTED).build()

    /** The sync that runs while the app is open: allowed on Wi-Fi always, on mobile data only when the switch is off. */
    fun allowedNow(context: Context): Boolean {
        if (!wifiOnly(context)) return true
        val metered = context.getSystemService<ConnectivityManager>()?.isActiveNetworkMetered ?: return true
        return !metered
    }
}
