package app.coshell.notices

import android.content.Context
import app.coshell.core.domain.notices.NoticeHistory
import app.coshell.core.domain.notices.NoticeKind
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.LocalDate

/** What went out and when (#745): the day of the last optional notice and, per kind, its day and key. Phone-only, not in the backup. */
@Singleton
class NoticeLog @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("notice_log", Context.MODE_PRIVATE)

    fun history(): NoticeHistory {
        val perKind = NoticeKind.entries.mapNotNull { kind ->
            val day = prefs.getString(dayKey(kind), null)?.let(::parse) ?: return@mapNotNull null
            kind to NoticeHistory.Shown(day, prefs.getString(keyKey(kind), "").orEmpty())
        }.toMap()
        return NoticeHistory(prefs.getString(LAST_ANY, null)?.let(::parse), perKind)
    }

    /** The keys of limit notices told this month (`LimitAlert.key`: category, month, kind): each limit hears of each kind once a month. */
    fun limitTold(): Set<String> = prefs.getStringSet(LIMIT_TOLD, null).orEmpty()

    fun record(kind: NoticeKind, key: String, day: LocalDate) {
        val edit = prefs.edit().putString(LAST_ANY, day.toString()).putString(dayKey(kind), day.toString()).putString(keyKey(kind), key)
        if (kind == NoticeKind.LIMIT) {
            // Keys of other months are dropped: `LimitAlert.key` is «category:month:kind».
            val month = key.split(':').getOrNull(1)
            edit.putStringSet(LIMIT_TOLD, limitTold().filter { it.split(':').getOrNull(1) == month }.toSet() + key)
        }
        edit.apply()
    }

    private fun parse(text: String): LocalDate? = runCatching { LocalDate.parse(text) }.getOrNull()

    private companion object {
        const val LAST_ANY = "last_any"
        const val LIMIT_TOLD = "limit_told"
        fun dayKey(kind: NoticeKind) = "day_${kind.key}"
        fun keyKey(kind: NoticeKind) = "key_${kind.key}"
    }
}
