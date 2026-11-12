package app.coshell.update.log

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Instant

/** [UpdateJournal] in SharedPreferences: survives the app being replaced by the new version. */
@Singleton
class PrefsUpdateJournal @Inject constructor(
    @ApplicationContext context: Context,
    private val clock: Clock,
) : UpdateJournal {

    private val prefs by lazy { context.getSharedPreferences("update_journal", Context.MODE_PRIVATE) }

    @Synchronized
    override fun add(event: String) {
        val now = clock.now().let { Instant.fromEpochSeconds(it.epochSeconds) }
        val line = "$now ${event.replace('\n', ' ')}"
        prefs.edit { putString(KEY_LINES, appendCapped(lines(), line).joinToString("\n")) }
    }

    @Synchronized
    override fun lines(): List<String> =
        prefs.getString(KEY_LINES, null)?.split('\n')?.filter { it.isNotBlank() }.orEmpty()

    override var preferManualInstall: Boolean
        get() = prefs.getBoolean(KEY_MANUAL, false)
        set(value) = prefs.edit { putBoolean(KEY_MANUAL, value) }

    private companion object {
        const val KEY_LINES = "lines"
        const val KEY_MANUAL = "prefer_manual_install"
    }
}
