package app.coshell.currencies.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.data.repository.RoomFamilySettings
import app.coshell.core.domain.settings.ReportCurrencies
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Before #122 report currencies lived in the device "settings" file as `"USD,ARS"`. At start-up the
 * old list moves once into the family settings ([RoomFamilySettings.importLegacyReportCurrencies]),
 * then the old key is removed so it never comes back.
 */
class LegacyReportCurrencies @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: RoomFamilySettings,
) {
    suspend fun moveToFamilySettings() {
        val prefs = withContext(Dispatchers.IO) { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
        val stored = prefs.getString(KEY, null) ?: return
        settings.importLegacyReportCurrencies(ReportCurrencies.decode(stored))
        prefs.edit { remove(KEY) }
    }

    private companion object {
        const val KEY = "report_currencies"
    }
}
