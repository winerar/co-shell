package app.coshell.notices.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.notices.NoticeKind
import app.coshell.core.domain.notices.NoticeSettings
import app.coshell.core.domain.settings.ClockMinutes
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
    private val timeState = MutableStateFlow<Int?>(null)
    private val offerState = MutableStateFlow<Boolean?>(null)

    override val enabled: Flow<Set<NoticeKind>> = kindsState
        .onStart { if (kindsState.value == null) kindsState.compareAndSet(null, io { storedKinds(prefs) }) }
        .filterNotNull()

    override suspend fun setEnabled(kind: NoticeKind, on: Boolean) {
        val now = (kindsState.value ?: io { storedKinds(prefs) }).let { if (on) it + kind else it - kind }
        kindsState.value = now
        io { prefs.edit { putStringSet(KEY_KINDS, now.map { it.key }.toSet()) } }
    }

    override val time: Flow<Int> = timeState
        .onStart { if (timeState.value == null) timeState.compareAndSet(null, io { storedTime(prefs) }) }
        .filterNotNull()

    override suspend fun setTime(minutes: Int) {
        timeState.value = minutes
        io { prefs.edit { putInt(KEY_TIME, minutes) } }
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
        /** Before #777 only whole hours: read once as a fallback for [KEY_TIME]. */
        private const val KEY_HOUR = "notice_hour"
        private const val KEY_TIME = "notice_time"
        private const val KEY_OFFER = "notice_offer_answered"

        private fun storedKinds(prefs: android.content.SharedPreferences): Set<NoticeKind> =
            prefs.getStringSet(KEY_KINDS, null).orEmpty().mapNotNull { NoticeKind.fromKey(it) }.toSet()

        private fun storedTime(prefs: android.content.SharedPreferences): Int =
            prefs.getInt(KEY_TIME, -1).takeIf { ClockMinutes.isValid(it) }
                ?: prefs.getInt(KEY_HOUR, -1).takeIf { it in 0..23 }?.let { ClockMinutes.of(it) }
                ?: NoticeSettings.DEFAULT_TIME

        /** The time for the scheduler, which starts without Hilt. */
        fun readTime(context: Context): Int = storedTime(context.getSharedPreferences(FILE, Context.MODE_PRIVATE))
    }
}
