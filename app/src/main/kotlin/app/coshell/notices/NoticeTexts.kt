package app.coshell.notices

import android.content.Context
import app.coshell.R
import app.coshell.core.domain.notices.NoticeFacts
import app.coshell.core.domain.notices.NoticeKind
import app.coshell.core.domain.notices.PlannedNotice
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.daysUntil

/** The words of each notification (#745, board `Not745TextA1/A2`): one fact and a soft phrase, no blame, no red. Resources speak the app's language. */
object NoticeTexts {
    fun of(context: Context, planned: PlannedNotice, facts: NoticeFacts): NoticeContent {
        val res = context.resources
        return when (planned.kind) {
            NoticeKind.EVENING -> NoticeContent(
                res.getString(R.string.notice_evening_title), res.getString(R.string.notice_evening_text),
                NoticeTarget.NEW_ENTRY, res.getString(R.string.notice_action_write),
            )
            NoticeKind.COMEBACK -> NoticeContent(
                res.getString(R.string.notice_comeback_title), res.getString(R.string.notice_comeback_text),
                NoticeTarget.NEW_ENTRY, res.getString(R.string.notice_action_write),
            )
            NoticeKind.LIMIT -> {
                val limit = checkNotNull(planned.limit)
                NoticeContent(
                    res.getString(R.string.notice_limit_title, limit.name, limit.left),
                    res.getQuantityString(R.plurals.notice_limit_text, limit.daysLeft, limit.percentLeft, limit.daysLeft),
                    NoticeTarget.BUDGETS, res.getString(R.string.notice_action_open),
                )
            }
            NoticeKind.RECAP -> NoticeContent(
                res.getString(R.string.notice_recap_title, lastMonthName(context, facts)), res.getString(R.string.notice_recap_text),
                NoticeTarget.RECAP, res.getString(R.string.notice_action_open),
            )
            NoticeKind.BACKUP -> NoticeContent(
                res.getString(R.string.notice_backup_title), backupText(context, facts),
                NoticeTarget.BACKUP, res.getString(R.string.notice_action_save),
            )
            NoticeKind.DRAFTS -> NoticeContent(
                res.getString(R.string.notice_drafts_title), res.getString(R.string.notice_drafts_text, facts.readyDrafts),
                NoticeTarget.DRAFTS, res.getString(R.string.notice_action_open),
            )
        }
    }

    private fun backupText(context: Context, facts: NoticeFacts): String {
        val last = facts.lastBackupDay ?: return context.getString(R.string.notice_backup_text_never)
        val days = last.daysUntil(facts.today)
        return context.resources.getQuantityString(R.plurals.notice_backup_text_last, days, days)
    }

    /** «ноября»: the month that just ended, in the form that follows «Итоги». */
    private fun lastMonthName(context: Context, facts: NoticeFacts): String {
        val previous = facts.today.let { if (it.monthNumber == 1) 12 else it.monthNumber - 1 }
        return Month.of(previous).getDisplayName(TextStyle.FULL, context.resources.configuration.locales[0] ?: Locale.getDefault())
    }
}
