package app.coshell.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellChoicePill
import app.coshell.core.designsystem.components.CoShellCounter
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ClockMinutes
import app.coshell.core.domain.settings.ReminderSettings
import app.coshell.ui.format.formatClock
import app.coshell.ui.sheet.FormSheet
import kotlinx.datetime.LocalDateTime

/**
 * «Уведомления»: a time or «Предупреждать» as pill buttons with «Другое…» last (#777, poll 2026-11-27 «А», board `Set1Notices`).
 * «Другое…» opens a small sheet with [CoShellCounter]; a value that is not a button takes the place of «Другое…» and stays chosen.
 */
@Composable
internal fun TimePickCard(title: String, time: Int, presets: List<Int>, onTime: (Int) -> Unit) {
    var picking by rememberSaveable { mutableStateOf(false) }
    PillsCard(title, presets.map { clockText(it) }, presets.indexOf(time), custom = clockText(time).takeIf { time !in presets }, onPick = { onTime(presets[it]) }) {
        picking = true
    }
    if (picking) {
        CounterSheet(
            title = title,
            start = time,
            step = { v, n -> ClockMinutes.step(v, n) },
            text = { clockText(it) },
            caption = stringResource(R.string.settings_notices_time_step),
            minusLabel = stringResource(R.string.settings_notices_earlier),
            plusLabel = stringResource(R.string.settings_notices_later),
            mono = true,
            onDone = onTime,
            onClosed = { picking = false },
        )
    }
}

@Composable
internal fun DaysPickCard(daysBefore: Int, onDays: (Int) -> Unit) {
    var picking by rememberSaveable { mutableStateOf(false) }
    val presets = ReminderSettings.DAYS_BEFORE
    val title = stringResource(R.string.settings_notices_before)
    PillsCard(title, presets.map { daysText(it) }, presets.indexOf(daysBefore), custom = daysText(daysBefore).takeIf { daysBefore !in presets }, onPick = { onDays(presets[it]) }) {
        picking = true
    }
    if (picking) {
        CounterSheet(
            title = title,
            start = daysBefore,
            step = { v, n -> ReminderSettings.stepDays(v, n) },
            text = { daysText(it) },
            caption = stringResource(R.string.settings_notices_days_caption),
            minusLabel = stringResource(R.string.settings_notices_fewer),
            plusLabel = stringResource(R.string.settings_notices_more),
            canMinus = { it > 0 },
            canPlus = { it < ReminderSettings.MAX_DAYS_BEFORE },
            onDone = onDays,
            onClosed = { picking = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PillsCard(title: String, labels: List<String>, selected: Int, custom: String?, onPick: (Int) -> Unit, onOther: () -> Unit) {
    CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
        row {
            Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.m)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                FlowRow(
                    Modifier.fillMaxWidth().padding(top = Spacing.m),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    labels.forEachIndexed { i, label -> CoShellChoicePill(label, selected = i == selected, onClick = { onPick(i) }) }
                    CoShellChoicePill(custom ?: stringResource(R.string.settings_notices_other), selected = custom != null, onClick = onOther)
                }
            }
        }
    }
}

@Composable
private fun CounterSheet(
    title: String,
    start: Int,
    step: (Int, Int) -> Int,
    text: @Composable (Int) -> String,
    caption: String,
    minusLabel: String,
    plusLabel: String,
    onDone: (Int) -> Unit,
    onClosed: () -> Unit,
    canMinus: (Int) -> Boolean = { true },
    canPlus: (Int) -> Boolean = { true },
    mono: Boolean = false,
) {
    var value by rememberSaveable { mutableIntStateOf(start) }
    FormSheet(title, onClosed, onAction = { close -> onDone(value); close() }) {
        CoShellCounter(
            value = text(value),
            onStep = { value = step(value, it) },
            minusLabel = minusLabel,
            plusLabel = plusLabel,
            caption = caption,
            canMinus = canMinus(value),
            canPlus = canPlus(value),
            mono = mono,
            modifier = Modifier.padding(vertical = Spacing.m),
        )
    }
}

/** «9:30» from minutes since midnight. */
internal fun clockText(minutes: Int): String =
    formatClock(LocalDateTime(2000, 1, 1, ClockMinutes.hour(minutes), ClockMinutes.minute(minutes)))

@Composable
internal fun daysText(days: Int): String =
    if (days == 0) stringResource(R.string.settings_notices_same_day) else pluralStringResource(R.plurals.settings_notices_days, days, days)
