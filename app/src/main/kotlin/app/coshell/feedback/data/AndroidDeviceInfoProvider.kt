package app.coshell.feedback.data

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import app.coshell.BuildConfig
import app.coshell.core.domain.feedback.DeviceInfo
import app.coshell.core.domain.feedback.DeviceInfoProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** App version, phone model, Android version and a random installation id kept in SharedPreferences. */
@Singleton
class AndroidDeviceInfoProvider @Inject constructor(@ApplicationContext context: Context) : DeviceInfoProvider {

    private val prefs by lazy { context.getSharedPreferences("installation", Context.MODE_PRIVATE) }

    override suspend fun deviceInfo() = DeviceInfo(
        appVersion = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        device = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
        os = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        installationId = installationId(),
    )

    private suspend fun installationId(): String = withContext(Dispatchers.IO) {
        prefs.getString(KEY_ID, null) ?: UUID.randomUUID().toString().also { id -> prefs.edit { putString(KEY_ID, id) } }
    }

    private companion object {
        const val KEY_ID = "id"
    }
}
