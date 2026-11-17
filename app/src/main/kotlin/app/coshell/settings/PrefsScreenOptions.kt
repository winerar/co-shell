package app.coshell.settings

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.settings.HomeLayout
import app.coshell.core.domain.settings.ReportsStart
import app.coshell.core.domain.settings.WeekStart
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.core.domain.settings.ScreenOptionsStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [ScreenOptionsStore] in the small «settings» SharedPreferences file (a device preference, not part of the backup). */
@Singleton
class PrefsScreenOptions @Inject constructor(@ApplicationContext context: Context) : ScreenOptionsStore {

    private val prefs by lazy { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<ScreenOptions?>(null)

    override val options: Flow<ScreenOptions> = state
        .onStart { if (state.value == null) state.compareAndSet(null, io { read() }) }
        .filterNotNull()

    override suspend fun update(change: (ScreenOptions) -> ScreenOptions) {
        val current = state.value ?: io { read() }
        val next = change(current)
        state.value = next
        io { write(next) }
    }

    private fun read(): ScreenOptions {
        val d = ScreenOptions()
        return ScreenOptions(
            opsShowAccount = prefs.getBoolean(OPS_ACCOUNT, d.opsShowAccount),
            opsShowTags = prefs.getBoolean(OPS_TAGS, d.opsShowTags),
            opsShowNote = prefs.getBoolean(OPS_NOTE, d.opsShowNote),
            accountsByCurrency = prefs.getBoolean(ACC_CURRENCY, d.accountsByCurrency),
            accountsShowArchived = prefs.getBoolean(ACC_ARCHIVED, d.accountsShowArchived),
            reportsStart = ReportsStart.parse(prefs.getString(REP_START, null)),
            reportsCompare = prefs.getBoolean(REP_COMPARE, d.reportsCompare),
            haptics = prefs.getBoolean(HAPTICS, d.haptics),
            syncWifiOnly = prefs.getBoolean(SYNC_WIFI, d.syncWifiOnly),
            home = HomeLayout.parse(prefs.getString(HOME, null)),
            soonDays = prefs.getInt(SOON_DAYS, d.soonDays).takeIf { it in ScreenOptions.SOON_DAYS } ?: d.soonDays,
            entryAccountId = prefs.getString(ENTRY_ACCOUNT, null)?.takeIf { it.isNotEmpty() },
            weekStart = WeekStart.parse(prefs.getString(WEEK_START, null)),
            recentCount = prefs.getInt(RECENT_COUNT, d.recentCount).takeIf { it in ScreenOptions.RECENT_COUNTS } ?: d.recentCount,
        )
    }

    private fun write(o: ScreenOptions) = prefs.edit {
        putBoolean(OPS_ACCOUNT, o.opsShowAccount)
        putBoolean(OPS_TAGS, o.opsShowTags)
        putBoolean(OPS_NOTE, o.opsShowNote)
        putBoolean(ACC_CURRENCY, o.accountsByCurrency)
        putBoolean(ACC_ARCHIVED, o.accountsShowArchived)
        putString(REP_START, o.reportsStart.name)
        putBoolean(REP_COMPARE, o.reportsCompare)
        putBoolean(HAPTICS, o.haptics)
        putBoolean(SYNC_WIFI, o.syncWifiOnly)
        putString(HOME, o.home.encode())
        putInt(SOON_DAYS, o.soonDays)
        putInt(RECENT_COUNT, o.recentCount)
        putString(WEEK_START, o.weekStart.name)
        putString(ENTRY_ACCOUNT, o.entryAccountId.orEmpty())
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    companion object {
        /** The file and key the background sync reads without Hilt (`SyncNetwork`). */
        const val FILE = "settings"
        const val KEY_SYNC_WIFI = "sync_wifi_only"

        private const val OPS_ACCOUNT = "ops_account"
        private const val OPS_TAGS = "ops_tags"
        private const val OPS_NOTE = "ops_note"
        private const val ACC_CURRENCY = "acc_by_currency"
        private const val ACC_ARCHIVED = "acc_archived"
        private const val REP_START = "rep_start"
        private const val REP_COMPARE = "rep_compare"
        private const val ENTRY_ACCOUNT = "entry_account"
        private const val WEEK_START = "week_start"
        private const val HOME = "home_layout"
        private const val SOON_DAYS = "home_soon_days"
        private const val RECENT_COUNT = "home_recent_count"
        private const val HAPTICS = "haptics"
        private const val SYNC_WIFI = KEY_SYNC_WIFI
    }
}
