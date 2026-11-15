package app.coshell.templates.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.PostMode
import app.coshell.recurring.recurrenceText
import app.coshell.templates.edit.TemplateEditActions
import app.coshell.templates.edit.TemplateEditUiState
import app.coshell.ui.format.formatWeekdayDayMonth

/**
 * «Детали» of the template (#143, board `Rec143EditA`): the «Повтор» tile and, once it is set, «Как проводить» and
 * «Следующий». Every tile opens the «Повтор» sheet ([RepeatSheet]); without a repeat the template works as before.
 */
@Composable
internal fun RepeatBlock(state: TemplateEditUiState, actions: TemplateEditActions) {
    var open by rememberSaveable { mutableStateOf(false) }
    val schedule = state.schedule
    CoShellFormSection(title = stringResource(R.string.entry_section_details)) {
        val tiles = buildList<@Composable (Modifier) -> Unit> {
            add { m ->
                CoShellDetailTile(
                    painterResource(DesignR.drawable.ic_refresh), stringResource(R.string.recurring_repeat),
                    recurrenceText(schedule?.recurrence), { open = true }, m, empty = schedule == null,
                )
            }
            if (schedule != null) {
                add { m ->
                    CoShellDetailTile(
                        painterResource(DesignR.drawable.ic_bolt), stringResource(R.string.recurring_how),
                        stringResource(if (state.postMode == PostMode.AUTO) R.string.recurring_auto else R.string.recurring_remind), { open = true }, m,
                    )
                }
                state.nextPayment?.let { next ->
                    add { m ->
                        val date = formatWeekdayDayMonth(next)
                        val missed = state.today?.let { next < it } == true
                        CoShellDetailTile(
                            painterResource(DesignR.drawable.ic_calendar_today), stringResource(R.string.recurring_next),
                            if (missed) stringResource(R.string.recurring_missed_value, date) else date, { open = true }, m,
                        )
                    }
                }
            }
        }
        tiles.chunked(2).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s - Spacing.xxs)) {
                line.forEach { tile -> tile(Modifier.weight(1f)) }
                if (line.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
    if (open) RepeatSheet(state, onDone = actions.onRepeatChange, onDismiss = { open = false })
}
