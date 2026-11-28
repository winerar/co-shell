package app.coshell.reports.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellReorderColumn
import app.coshell.core.designsystem.components.rememberReorderState
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ReportCard
import app.coshell.core.domain.settings.ReportLayout
import app.coshell.ui.bar.aboveBottomBars

// The editing mode of «Отчёты» (#826, poll 2026-11-28 «А», board Rep826Compact): held a card — the feed folds into a list of
// names, all of them on about one screen; «Готово» (or «Назад») unfolds it. Every change is saved at once.

/** The list itself, under the header; [padding] — the Scaffold's. */
@Composable
internal fun ReportsEditList(layout: ReportLayout, padding: PaddingValues, onChange: (ReportLayout) -> Unit, onDone: () -> Unit) {
    BackHandler(onBack = onDone)
    val state = rememberReorderState()
    CoShellReorderColumn(
        state = state,
        keys = layout.visible,
        onDrop = { drop -> (drop.key as? ReportCard)?.let { onChange(layout.moveShown(it, drop.index)) } },
        // «Готово» below ends the list (as on «Обзор»): the column stops above it instead of passing under it.
        modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
        spacing = Spacing.xxs,
        contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, top = padding.calculateTopPadding() + Spacing.s, bottom = Spacing.l),
    ) {
        Text(stringResource(R.string.report_cards_help), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ReportCardRows(layout, state, onChange)
    }
}

/** «Готово» under the list (the Scaffold's bottom bar while editing). */
@Composable
internal fun ReportsEditDone(onDone: () -> Unit) {
    Column(Modifier.fillMaxWidth().aboveBottomBars().padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.keypad_done)) }
    }
}
