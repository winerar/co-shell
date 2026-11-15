package app.coshell.budgets.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellDayGrid
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.format.formatMonthYear
import app.coshell.ui.sheet.ModalSheet
import kotlinx.datetime.YearMonth
import app.coshell.core.designsystem.R as DesignR

/**
 * «Начало месяца» (#213, #345 — mockup `FbModalADay`): the day the budget month starts on, 1..31 (#139), in a grid;
 * under it the period it gives for [month]. Days past the end of a short month fall on its last day (`budgetPeriod`).
 * «Готово» applies the day ([onPick] also closes the sheet).
 */
@Composable
internal fun MonthStartSheet(current: Int, month: YearMonth?, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    var day by rememberSaveable { mutableIntStateOf(current) }
    ModalSheet(
        title = stringResource(R.string.budgets_start_title),
        subtitle = stringResource(R.string.budgets_start_subtitle),
        actionLabel = stringResource(R.string.keypad_done),
        onAction = { onPick(day) },
        onClosed = onDismiss,
    ) {
        CoShellDayGrid(selected = day, onSelect = { day = it }, markedDay = MonthStartSettings.DEFAULT)
        month?.let { PeriodLine(it, day) }
        CoShellHint(stringResource(R.string.budgets_start_rule))
    }
}

@Composable
private fun PeriodLine(month: YearMonth, day: Int) {
    val period = budgetPeriod(month, day)
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.medium).padding(Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(DesignR.drawable.ic_calendar_today), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            stringResource(R.string.budgets_start_period, formatMonthYear(month), formatDayMonth(period.start), formatDayMonth(period.lastDay)),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
