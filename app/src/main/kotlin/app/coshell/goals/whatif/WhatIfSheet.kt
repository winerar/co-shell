package app.coshell.goals.whatif

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.goals.WHAT_IF_DEFAULT_SHARE
import app.coshell.core.domain.goals.WHAT_IF_SHARES
import app.coshell.goals.GoalWhatIfUi
import app.coshell.reports.categoryName
import app.coshell.ui.sheet.ModalSheet

/**
 * «Что если» (#792, board `Smart792Sheet`): which category, how much less — and the month the goal is reached now and with the
 * saving. A guess: nothing is saved or changed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WhatIfSheet(whatIf: GoalWhatIfUi, onClosed: () -> Unit) {
    var category by rememberSaveable(whatIf.goalId) { mutableStateOf(whatIf.line.categoryId) }
    var share by rememberSaveable(whatIf.goalId) { mutableIntStateOf(WHAT_IF_SHARES.indexOf(WHAT_IF_DEFAULT_SHARE)) }
    ModalSheet(title = stringResource(R.string.goal_whatif_title), onClosed = onClosed, subtitle = stringResource(R.string.goal_whatif_subtitle, whatIf.goalName, whatIf.target)) {
        Label(stringResource(R.string.goal_whatif_category))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            whatIf.categories.forEach { option ->
                CoShellFilterChip(selected = option.id == category, onClick = { category = option.id }, label = { Text(categoryName(option.name)) })
            }
        }
        Label(stringResource(R.string.goal_whatif_share))
        CoShellSegmented(WHAT_IF_SHARES.map { stringResource(R.string.goal_whatif_percent, it) }, selected = share, onSelect = { share = it })
        val result = whatIf.results[GoalWhatIfUi.key(category, WHAT_IF_SHARES[share])]
        if (result == null) {
            Text(stringResource(R.string.goal_whatif_nothing), style = MaterialTheme.typography.bodyMedium)
        } else {
            Line(stringResource(R.string.goal_whatif_saved)) { AmountText(stringResource(R.string.goals_approx, result.saved), AmountTextStyle) }
            Line(stringResource(R.string.goal_whatif_now)) { Text(result.now ?: stringResource(R.string.goal_whatif_not_closer)) }
            Line(stringResource(R.string.goal_whatif_with)) { Text(result.with, style = MaterialTheme.typography.titleSmall) }
        }
        Label(stringResource(R.string.goal_whatif_note))
    }
}

@Composable
private fun Label(text: String) = Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Line(label: String, value: @Composable () -> Unit) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.s), itemVerticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) { value() }
    }
}
