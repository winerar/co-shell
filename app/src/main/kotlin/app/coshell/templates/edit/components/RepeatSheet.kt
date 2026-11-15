package app.coshell.templates.edit.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellDayGrid
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellLabelGrid
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRadioMark
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.domain.recurring.nextUnhandled
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.templates.edit.TemplateEditUiState
import app.coshell.ui.format.formatMonthShort
import app.coshell.ui.format.formatWeekdayDayMonth
import app.coshell.ui.format.formatWeekdayShort
import app.coshell.ui.sheet.ModalSheet
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.YearMonth

/**
 * «Повтор» (#143, board `Rec143RepeatA`): «Нет · Месяц · Неделя · Год», the day for the chosen frequency, «Как проводить»
 * and, under it, when the next payment comes. «Готово» hands the rule to [onDone]; the sheet never scrolls.
 */
@Composable
internal fun RepeatSheet(state: TemplateEditUiState, onDone: (Recurrence?, PostMode) -> Unit, onDismiss: () -> Unit) {
    val today = state.today ?: return
    var draft by rememberSaveable(stateSaver = RepeatDraft.Saver) {
        mutableStateOf(RepeatDraft.from(state.schedule?.recurrence, state.postMode, today))
    }
    ModalSheet(
        title = stringResource(R.string.recurring_repeat),
        subtitle = sheetSubtitle(state),
        actionLabel = stringResource(R.string.keypad_done),
        onAction = { close ->
            onDone(draft.recurrence, draft.mode.takeIf { state.canPostAuto } ?: PostMode.REMIND)
            close()
        },
        onClosed = onDismiss,
    ) {
        RepeatSheetBody(draft, state, onChange = { draft = it })
    }
}

/** The inside of [RepeatSheet] — also for the screenshot reference. */
@Composable
internal fun ColumnScope.RepeatSheetBody(draft: RepeatDraft, state: TemplateEditUiState, onChange: (RepeatDraft) -> Unit) {
    CoShellSegmented(
        labels = listOf(R.string.recurring_off, R.string.recurring_month, R.string.recurring_week, R.string.recurring_year).map { stringResource(it) },
        selected = draft.kind.ordinal,
        onSelect = { onChange(draft.copy(kind = RepeatKind.entries[it])) },
    )
    when (draft.kind) {
        RepeatKind.NONE -> Unit
        RepeatKind.MONTH -> {
            CoShellSectionLabel(stringResource(R.string.recurring_day_of_month))
            CoShellDayGrid(selected = draft.monthDay, onSelect = { onChange(draft.copy(monthDay = it)) })
        }
        RepeatKind.WEEK -> {
            CoShellSectionLabel(stringResource(R.string.recurring_weekday))
            CoShellLabelGrid(
                labels = DayOfWeek.entries.map { formatWeekdayShort(it) },
                selected = draft.weekday.ordinal,
                onSelect = { onChange(draft.copy(weekday = DayOfWeek.entries[it])) },
                columns = DayOfWeek.entries.size,
            )
        }
        RepeatKind.YEAR -> {
            CoShellSectionLabel(stringResource(R.string.recurring_month_of_year))
            CoShellLabelGrid(
                labels = (1..MONTHS).map { formatMonthShort(YearMonth(LEAP_YEAR, it)) },
                selected = draft.yearMonth - 1,
                onSelect = { onChange(draft.copy(yearMonth = it + 1)) },
                columns = MONTHS / 2,
            )
            val days = RepeatDraft.daysInMonth(draft.yearMonth)
            CoShellDayGrid(selected = minOf(draft.yearDay, days), onSelect = { onChange(draft.copy(yearDay = it)) }, days = days)
        }
    }
    if (draft.kind != RepeatKind.NONE) {
        CoShellSectionLabel(stringResource(R.string.recurring_how))
        ModeRows(draft.mode.takeIf { state.canPostAuto } ?: PostMode.REMIND, state) { onChange(draft.copy(mode = it)) }
    }
    CoShellHint(hintText(draft, state))
}

@Composable
private fun ModeRows(mode: PostMode, state: TemplateEditUiState, onPick: (PostMode) -> Unit) {
    val autoHint = when {
        state.isTransfer -> R.string.recurring_auto_no_transfer
        !state.canPostAuto -> R.string.recurring_auto_needs_amount
        else -> R.string.recurring_auto_hint
    }
    CoShellRowGroup {
        row {
            CoShellListRow(
                title = stringResource(R.string.recurring_remind),
                subtitle = stringResource(R.string.recurring_remind_hint),
                trailing = { CoShellRadioMark(selected = mode == PostMode.REMIND) },
                onClick = { onPick(PostMode.REMIND) },
            )
        }
        row {
            CoShellListRow(
                title = stringResource(R.string.recurring_auto),
                subtitle = stringResource(autoHint),
                trailing = { CoShellRadioMark(selected = mode == PostMode.AUTO) },
                onClick = { onPick(PostMode.AUTO) },
                enabled = state.canPostAuto,
            )
        }
    }
}

@Composable
private fun hintText(draft: RepeatDraft, state: TemplateEditUiState): String {
    val recurrence = draft.recurrence ?: return stringResource(R.string.recurring_off_hint)
    val next = state.preview(recurrence, draft.mode)?.nextUnhandled()?.let { stringResource(R.string.recurring_next_payment, formatWeekdayDayMonth(it)) }
    val shortMonth = stringResource(R.string.recurring_short_month).takeIf { recurrence is Recurrence.Monthly && recurrence.day > SAFE_DAY }
    return listOfNotNull(shortMonth, next).joinToString(" ")
}

/** «Аренда · 650 USD»; the name alone when the amount is asked every time. */
@Composable
private fun sheetSubtitle(state: TemplateEditUiState): String? {
    val name = state.name.trim().ifEmpty { return null }
    val amount = state.amount.trim().takeIf { it.isNotEmpty() } ?: return name
    return stringResource(R.string.template_account_value, name, listOfNotNull(amount, state.account?.currency).joinToString(" "))
}

/** Every month has it: no «last day» note needed. */
private const val SAFE_DAY = 28
private const val MONTHS = 12

/** Any leap year: month names only. */
private const val LEAP_YEAR = 2028

