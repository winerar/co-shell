package app.coshell.hints

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.annotation.StringRes
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellCheckMark
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.hints.FirstStep
import app.coshell.core.domain.hints.FirstStepsState
import app.coshell.ui.sheet.ModalSheet

@get:StringRes
private val FirstStep.title: Int
    get() = when (this) {
        FirstStep.ACCOUNT -> R.string.steps_account
        FirstStep.ENTRY -> R.string.steps_entry
        FirstStep.LIMIT -> R.string.steps_limit
        FirstStep.FAMILY -> R.string.steps_family
    }

@get:StringRes
private val FirstStep.hint: Int
    get() = when (this) {
        FirstStep.ACCOUNT -> R.string.steps_account_hint
        FirstStep.ENTRY -> R.string.steps_entry_hint
        FirstStep.LIMIT -> R.string.steps_limit_hint
        FirstStep.FAMILY -> R.string.steps_family_hint
    }

/** Row on «Обзор» (board `Hint513Home0`): «Первые шаги», what is done and what is next, a meter; a tap opens [FirstStepsSheet]. */
@Composable
fun FirstStepsRow(state: FirstStepsState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val next = state.next
    val sub = if (state.doneCount == 0 || next == null) {
        stringResource(R.string.steps_row_start)
    } else {
        stringResource(R.string.steps_row_progress, state.doneCount, state.total, stringResource(next.title).lowercase())
    }
    CoShellCard(modifier.fillMaxWidth(), onClick = onClick) {
        CoShellMenuRow(
            icon = DesignR.drawable.ic_lightbulb,
            title = stringResource(R.string.steps_title),
            subtitle = sub,
            onClick = onClick,
        )
        CoShellMeter(
            fraction = state.doneCount / state.total.toFloat(),
            color = progressColors().indicator,
            modifier = Modifier.fillMaxWidth().padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.m),
        )
    }
}

/** The list of steps (board `Hint513Steps`): ticks by data, any step can be skipped, «Скрыть шаги» removes the row. */
@Composable
fun FirstStepsSheet(state: FirstStepsState, onHide: () -> Unit, onClosed: () -> Unit) {
    ModalSheet(
        title = stringResource(R.string.steps_title),
        subtitle = stringResource(R.string.steps_sheet_sub, state.doneCount, state.total),
        onClosed = onClosed,
    ) { close ->
        Column {
            CoShellRowGroup {
                FirstStep.entries.forEach { step ->
                    row {
                        CoShellListRow(
                            title = stringResource(step.title),
                            subtitle = stringResource(step.hint),
                            leading = { CoShellCheckMark(if (step in state.done) ToggleableState.On else ToggleableState.Off) },
                        )
                    }
                }
            }
            CoShellTextButton(onClick = { onHide(); close() }, modifier = Modifier.padding(top = Spacing.m)) { Text(stringResource(R.string.steps_hide)) }
        }
    }
}
