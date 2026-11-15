package app.coshell.reports.review.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.settings.MonthReviewSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.datetime.YearMonth

/** [MonthReviewSettings] in the device settings file (not synced, not in the backup: each phone hides its own card). */
@Singleton
class PrefsMonthReviewSettings @Inject constructor(@ApplicationContext context: Context) : MonthReviewSettings {

    private val prefs by lazy { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<Stored?>(null)

    override val dismissed: Flow<YearMonth?> = state
        .onStart { if (state.value == null) state.compareAndSet(null, Stored(io { MonthReviewSettings.parse(prefs.getString(KEY, null)) })) }
        .filterNotNull()
        .map { it.month }

    override suspend fun dismiss(month: YearMonth) {
        state.value = Stored(month)
        io { prefs.edit { putString(KEY, month.toString()) } }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private data class Stored(val month: YearMonth?)

    private companion object {
        const val KEY = "month_review_dismissed"
    }
}
