package app.coshell.notices.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.notices.NoticeKind
import app.coshell.core.domain.notices.NoticeSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [NoticeSettings] in the device's `settings` file, like `PrefsReminderSettings`. Nothing is on until the person turns it on. */
@Singleton
class PrefsNoticeSettings @Inject constructor(@ApplicationContext context: Context) : NoticeSettings {

    private val prefs by lazy { context.getSharedPreferences(FILE, Context.MODE_PRIVATE) }

    /** In-memory copies; `null` until the file is read once off the main thread. */
    private val kindsState = MutableStateFlow<Set<NoticeKind>?>(null)
    private val hourState = MutableStateFlow<Int?>(null)
    private val offerState = MutableStateFlow<Boolean?>(null)

    override val enabled: Flow<Set<NoticeKind>> = kindsState
        .onStart { if (kindsState.value == null) kindsState.compareAndSet(null, io { storedKinds(prefs) }) }
        .filterNotNull()

    override suspend fun setEnabled(kind: NoticeKind, on: Boolean) {
        val now = (kindsState.value ?: io { storedKinds(prefs) }).let { if (on) it + kind else it - kind }
        kindsState.value = now
        io { prefs.edit { putStringSet(KEY_KINDS, now.map { it.key }.toSet()) } }
    }

    override val hour: Flow<Int> = hourState
        .onStart { if (hourState.value == null) hourState.compareAndSet(null, io { storedHour(prefs) }) }
        .filterNotNull()

    override suspend fun setHour(hour: Int) {
        hourState.value = hour
        io { prefs.edit { putInt(KEY_HOUR, hour) } }
    }

    override val offerAnswered: Flow<Boolean> = offerState
        .onStart { if (offerState.value == null) offerState.compareAndSet(null, io { prefs.getBoolean(KEY_OFFER, false) }) }
        .filterNotNull()

    override suspend fun setOfferAnswered() {
        offerState.value = true
        io { prefs.edit { putBoolean(KEY_OFFER, true) } }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    companion object {
        private const val FILE = "settings"
        private const val KEY_KINDS = "notice_kinds"
        private const val KEY_HOUR = "notice_hour"
        private const val KEY_OFFER = "notice_offer_answered"

        private fun storedKinds(prefs: android.content.SharedPreferences): Set<NoticeKind> =
            prefs.getStringSet(KEY_KINDS, null).orEmpty().mapNotNull { NoticeKind.fromKey(it) }.toSet()

        private fun storedHour(prefs: android.content.SharedPreferences): Int =
            prefs.getInt(KEY_HOUR, NoticeSettings.DEFAULT_HOUR).takeIf { it in 0..23 } ?: NoticeSettings.DEFAULT_HOUR

        /** The hour for the scheduler, which starts without Hilt. */
        fun readHour(context: Context): Int = storedHour(context.getSharedPreferences(FILE, Context.MODE_PRIVATE))
    }
}
