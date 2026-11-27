package app.coshell.alarm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Whether the phone may keep the reminders from coming while the app is closed (#745, board `Set1Notices`). Xiaomi (HyperOS/MIUI)
 * does not start a sideloaded app by itself until «Автозапуск» is on, and the system cannot tell us that — so on Xiaomi the row is
 * always there; elsewhere — only while the battery optimization limits the app.
 */
object BackgroundCheck {
    enum class Advice { NONE, XIAOMI, BATTERY }

    private val XIAOMI = setOf("xiaomi", "redmi", "poco")

    /** Pure: what to say for this phone. */
    fun advice(manufacturer: String, ignoringBatteryOptimizations: Boolean): Advice = when {
        manufacturer.lowercase().trim() in XIAOMI -> Advice.XIAOMI
        !ignoringBatteryOptimizations -> Advice.BATTERY
        else -> Advice.NONE
    }

    /**
     * Ask «Разрешить работу в фоне?» when a reminder is turned on: while the battery limits the app — every time (it stops once
     * allowed); on Xiaomi, where we cannot tell, — only when it is the first reminder to be on ([anythingOnBefore] `false`).
     */
    fun askOnEnable(advice: Advice, anythingOnBefore: Boolean): Boolean = when (advice) {
        Advice.BATTERY -> true
        Advice.XIAOMI -> !anythingOnBefore
        Advice.NONE -> false
    }

    fun advice(context: Context): Advice = advice(
        Build.MANUFACTURER.orEmpty(),
        context.getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) ?: true,
    )

    /** The app's page in the phone's settings: «Автозапуск» and «Экономия батареи» (Xiaomi) or «Батарея» (Android 12+) are there. */
    fun settingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
