package app.coshell.ai.cloud

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.ai.CloudAiSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** How many requests the cloud still allows on [day] (UTC epoch day), as the Worker last said. */
data class CloudQuota(val left: Int, val limit: Int, val day: Long)

/**
 * «Облачный разбор» switch and the day's quota (#635): per phone, **off** by default, not synced and not in the backup (like `ThemeSettings`).
 */
@Singleton
class PrefsCloudAiSettings @Inject constructor(@ApplicationContext context: Context) : CloudAiSettings {
    private val prefs by lazy { context.getSharedPreferences("cloud_ai", Context.MODE_PRIVATE) }
    private val enabledState by lazy { MutableStateFlow(prefs.getBoolean(ENABLED, false)) }
    private val quotaState by lazy { MutableStateFlow(readQuota()) }

    val enabledFlow: StateFlow<Boolean> get() = enabledState
    val quota: StateFlow<CloudQuota?> get() = quotaState

    override val enabled: Boolean get() = enabledState.value

    fun setEnabled(value: Boolean) {
        prefs.edit { putBoolean(ENABLED, value) }
        enabledState.value = value
    }

    fun recordQuota(left: Int, limit: Int, day: Long) {
        prefs.edit { putInt(LEFT, left).putInt(LIMIT, limit).putLong(DAY, day) }
        quotaState.value = CloudQuota(left, limit, day)
    }

    private fun readQuota(): CloudQuota? =
        if (prefs.contains(DAY)) CloudQuota(prefs.getInt(LEFT, 0), prefs.getInt(LIMIT, 0), prefs.getLong(DAY, 0)) else null

    private companion object {
        const val ENABLED = "enabled"
        const val LEFT = "left"
        const val LIMIT = "limit"
        const val DAY = "day"
    }
}
