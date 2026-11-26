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

    fun record(kind: NoticeKind, key: String, day: LocalDate) {
        prefs.edit().putString(LAST_ANY, day.toString()).putString(dayKey(kind), day.toString()).putString(keyKey(kind), key).apply()
    }

    private fun parse(text: String): LocalDate? = runCatching { LocalDate.parse(text) }.getOrNull()

    private companion object {
        const val LAST_ANY = "last_any"
        fun dayKey(kind: NoticeKind) = "day_${kind.key}"
        fun keyKey(kind: NoticeKind) = "key_${kind.key}"
    }
}
