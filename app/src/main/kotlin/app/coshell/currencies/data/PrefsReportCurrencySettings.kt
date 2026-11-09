package app.coshell.currencies.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.settings.ReportCurrencies
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.CurrencyCode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [ReportCurrencySettings] in the device "settings" file as `"USD,ARS"` (moves to family settings in stage 4). */
@Singleton
class PrefsReportCurrencySettings @Inject constructor(
    @ApplicationContext context: Context,
) : ReportCurrencySettings {

    private val prefs by lazy { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<List<CurrencyCode>?>(null)

    override val currencies: Flow<List<CurrencyCode>> = state
        .onStart { if (state.value == null) state.compareAndSet(null, io { read() }) }
        .filterNotNull()

    override suspend fun setCurrencies(currencies: List<CurrencyCode>) {
        val clean = ReportCurrencies.normalize(currencies)
        state.value = clean
        io { prefs.edit { putString(KEY, clean.joinToString(",") { it.value }) } }
    }

    private fun read(): List<CurrencyCode> = ReportCurrencies.normalize(
        prefs.getString(KEY, null).orEmpty().split(',').mapNotNull { CurrencyCode.parseOrNull(it) },
    )

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val KEY = "report_currencies"
    }
}
