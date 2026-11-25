package app.coshell.backup.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.backup.auto.AutoBackupUiState
import app.coshell.backup.auto.BackupPeriod
import app.coshell.backup.auto.BackupSchedule
import app.coshell.backup.auto.clockOf
import app.coshell.core.designsystem.components.CoShellDayGrid
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellLabelGrid
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.settings.LocalScreenOptions
import app.coshell.settings.systemFirstDay
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.format.formatWeekdayShort
import app.coshell.core.designsystem.R as DesignR

/**
 * Automatic copy under the hero (boards Backup, BackupAuto; #606, board CopyA): the folder («нажмите, чтобы сменить») and a switch;
 * when it is on — «Каждый день / Раз в неделю / Раз в месяц», the weekday or the day of the month, the tiles «Время» and
 * «Хранить» (each opens a choice sheet) and «Следующая: …». Turning the switch on without a folder opens the folder picker.
 */
@Composable
internal fun AutoBackupSection(
    state: AutoBackupUiState,
    onChooseFolder: () -> Unit,
    onDisable: () -> Unit,
    onSchedule: (BackupSchedule) -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellRowGroup(Modifier.padding(horizontal = Spacing.l)) {
            row {
                CoShellMenuRow(
                    icon = DesignR.drawable.ic_folder_open,
                    title = state.folderName?.let { stringResource(R.string.auto_backup_folder, it) }
                        ?: stringResource(R.string.auto_backup_choose),
                    subtitle = if (state.folderName != null) null else stringResource(R.string.auto_backup_folder_hint),
                    onClick = onChooseFolder,
                )
            }
            row {
                CoShellSwitchRow(
                    title = stringResource(R.string.auto_backup_title),
                    checked = state.enabled,
                    onCheckedChange = { on -> if (on) onChooseFolder() else onDisable() },
                )
            }
        }
        if (state.enabled) ScheduleBlock(state, onSchedule)
    }
}

/** Period, day, time, how many to keep, the next copy: everything about «when» (board CopyA). */
@Composable
private fun ScheduleBlock(state: AutoBackupUiState, onSchedule: (BackupSchedule) -> Unit) {
    val schedule = state.schedule
    var sheet by rememberSaveable { mutableStateOf<ScheduleSheet?>(null) }
    val side = Modifier.padding(horizontal = Spacing.l)
    CoShellSegmented(
        labels = listOf(R.string.auto_backup_period_daily, R.string.auto_backup_period_weekly, R.string.auto_backup_period_monthly).map { stringResource(it) },
        selected = schedule.period.ordinal,
        onSelect = { onSchedule(schedule.copy(period = BackupPeriod.entries[it])) },
        modifier = side,
    )
    when (schedule.period) {
        BackupPeriod.DAILY -> Unit
        BackupPeriod.WEEKLY -> {
            // «Настройки → Деньги и даты → Неделя начинается с»: the days in the order of that week.
            val week = LocalScreenOptions.current.weekStart.days(systemFirstDay())
            CoShellLabelGrid(
                labels = week.map { formatWeekdayShort(it) },
                selected = week.indexOf(schedule.weekday),
                onSelect = { onSchedule(schedule.copy(weekday = week[it])) },
                modifier = side,
                columns = week.size,
            )
        }
        BackupPeriod.MONTHLY -> {
            CoShellDayGrid(selected = schedule.monthDay, onSelect = { onSchedule(schedule.copy(monthDay = it)) }, modifier = side)
            CoShellHint(stringResource(R.string.auto_backup_month_hint), side)
        }
    }
    CoShellRowGroup(side) {
        row { ValueRow(stringResource(R.string.auto_backup_time), clockOf(schedule.hour)) { sheet = ScheduleSheet.TIME } }
        row {
            ValueRow(
                stringResource(R.string.auto_backup_keep),
                pluralStringResource(R.plurals.auto_backup_keep_value, schedule.keep, schedule.keep),
            ) { sheet = ScheduleSheet.KEEP }
        }
    }
    CoShellHint(stringResource(R.string.auto_backup_next, state.nextDay, state.nextTime), side)
    when (sheet) {
        ScheduleSheet.TIME -> ChoiceSheet(
            title = stringResource(R.string.auto_backup_time),
            hint = stringResource(R.string.auto_backup_time_hint),
            options = (0 until HOURS).map { ChoiceOption(it, clockOf(it)) },
            selected = schedule.hour,
            onPick = { onSchedule(schedule.copy(hour = it)); sheet = null },
            onDismiss = { sheet = null },
        )
        ScheduleSheet.KEEP -> ChoiceSheet(
            title = stringResource(R.string.auto_backup_keep),
            hint = stringResource(R.string.auto_backup_keep_hint),
            options = BackupSchedule.KEEP_OPTIONS.map { ChoiceOption(it, pluralStringResource(R.plurals.auto_backup_keep_value, it, it)) },
            selected = schedule.keep,
            onPick = { onSchedule(schedule.copy(keep = it)); sheet = null },
            onDismiss = { sheet = null },
        )
        null -> Unit
    }
}

/** A tile of the board: the name on the left, the chosen value in grey on the right. */
@Composable
private fun ValueRow(title: String, value: String, onClick: () -> Unit) {
    CoShellListRow(
        title = title,
        onClick = onClick,
        trailing = { Text(value, style = MaterialTheme.typography.bodyMedium) },
    )
}

private enum class ScheduleSheet { TIME, KEEP }

private const val HOURS = 24

