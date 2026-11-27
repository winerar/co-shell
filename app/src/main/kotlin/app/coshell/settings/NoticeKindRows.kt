package app.coshell.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.notices.NoticeKind
import app.coshell.core.domain.notices.NoticeSettings
import app.coshell.ui.list.SectionTitle
import app.coshell.core.designsystem.R as DesignR

/** The groups of optional notifications in «Уведомления» (#745, board `Set1Notices`): a switch per kind, one time for the evening one. */
internal fun LazyListScope.noticeKindGroups(
    kinds: Set<NoticeKind>,
    onKind: (NoticeKind, Boolean) -> Unit,
    time: Int,
    onTime: (Int) -> Unit,
) {
    group(R.string.notice_group_spend, listOf(NoticeKind.EVENING), kinds, onKind)
    item(key = "notice_time") { TimePickCard(stringResource(R.string.notice_time_title), time, NoticeSettings.TIMES, onTime) }
    group(R.string.notice_group_budget, listOf(NoticeKind.LIMIT, NoticeKind.RECAP), kinds, onKind)
    group(R.string.notice_group_order, listOf(NoticeKind.BACKUP, NoticeKind.DRAFTS, NoticeKind.COMEBACK), kinds, onKind)
}

private fun LazyListScope.group(title: Int, group: List<NoticeKind>, on: Set<NoticeKind>, onKind: (NoticeKind, Boolean) -> Unit) {
    item(key = "title_$title") { SectionTitle(stringResource(title)) }
    item(key = "group_$title") {
        CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
            for (kind in group) {
                row {
                    CoShellSwitchRow(
                        leading = { MenuIcon(icon(kind)) },
                        title = stringResource(switchTitle(kind)),
                        subtitle = stringResource(switchHint(kind)),
                        checked = kind in on,
                        onCheckedChange = { onKind(kind, it) },
                    )
                }
            }
        }
    }
}

private fun icon(kind: NoticeKind): Int = when (kind) {
    NoticeKind.EVENING -> DesignR.drawable.ic_edit_note
    NoticeKind.LIMIT -> DesignR.drawable.ic_bar_chart
    NoticeKind.RECAP -> DesignR.drawable.ic_calendar_today
    NoticeKind.BACKUP -> DesignR.drawable.ic_settings_backup_restore
    NoticeKind.DRAFTS -> DesignR.drawable.ic_draft
    NoticeKind.COMEBACK -> DesignR.drawable.ic_history
}

private fun switchTitle(kind: NoticeKind): Int = when (kind) {
    NoticeKind.EVENING -> R.string.notice_evening_switch
    NoticeKind.LIMIT -> R.string.notice_limit_switch
    NoticeKind.RECAP -> R.string.notice_recap_switch
    NoticeKind.BACKUP -> R.string.notice_backup_switch
    NoticeKind.DRAFTS -> R.string.notice_drafts_switch
    NoticeKind.COMEBACK -> R.string.notice_comeback_switch
}

private fun switchHint(kind: NoticeKind): Int = when (kind) {
    NoticeKind.EVENING -> R.string.notice_evening_hint
    NoticeKind.LIMIT -> R.string.notice_limit_hint
    NoticeKind.RECAP -> R.string.notice_recap_hint
    NoticeKind.BACKUP -> R.string.notice_backup_hint
    NoticeKind.DRAFTS -> R.string.notice_drafts_hint
    NoticeKind.COMEBACK -> R.string.notice_comeback_hint
}
