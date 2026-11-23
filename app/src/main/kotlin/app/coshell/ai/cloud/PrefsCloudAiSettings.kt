package app.coshell.ai.cloud

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.ai.CloudAiSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** «Облачный разбор» switch (#635): per phone, **off** by default, not synced and not in the backup (like `ThemeSettings`). */
@Singleton
class PrefsCloudAiSettings @Inject constructor(@ApplicationContext context: Context) : CloudAiSettings {
    private val prefs by lazy { context.getSharedPreferences("cloud_ai", Context.MODE_PRIVATE) }

    override val enabled: Boolean get() = prefs.getBoolean(ENABLED, false)

    fun setEnabled(value: Boolean) = prefs.edit { putBoolean(ENABLED, value) }

    private companion object {
        const val ENABLED = "enabled"
    }
}
