package app.coshell.home.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.goals.GoalRow
import app.coshell.goals.GoalRowUi

/** «Цели» on «Обзоре» (#768, board `GoalHome`): the same rows as on «Цели»; «Все ›» opens the screen, a row — its goal. */
@Composable
internal fun GoalsHomeCard(rows: List<GoalRowUi>, onAll: () -> Unit, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(start = Spacing.xs), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            CoShellSectionLabel(stringResource(R.string.goals_title))
            CoShellSectionLink(stringResource(R.string.picker_all), onAll)
        }
        CoShellCard(Modifier.fillMaxWidth().padding(top = Spacing.s)) {
            rows.forEachIndexed { index, row ->
                key(row.id) {
                    if (index > 0) CoShellDivider(Modifier.padding(start = Spacing.l))
                    GoalRow(row, onClick = { onOpen(row.id) })
                }
            }
        }
    }
}
