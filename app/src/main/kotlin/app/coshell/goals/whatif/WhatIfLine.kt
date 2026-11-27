package app.coshell.goals.whatif

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.goals.GoalWhatIfUi
import app.coshell.goals.WhatIfResult
import app.coshell.reports.categoryName

/** «Если тратить на «Кафе» на 20 % меньше, цель будет на 3 месяца раньше. · Что если» under a goal (#792, poll «А», board `Smart792Line`). */
@Composable
internal fun WhatIfLine(whatIf: GoalWhatIfUi, onOpen: () -> Unit) {
    Column(Modifier.padding(start = Spacing.l, end = Spacing.s, bottom = Spacing.s)) {
        Text(whatIfSentence(whatIf.line), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        CoShellTextButton(onClick = onOpen) { Text(stringResource(R.string.goal_whatif_open)) }
    }
}

/** The sentence of [result]: sooner by whole months, or — when the goal does not get closer now — in how many months it would. */
@Composable
internal fun whatIfSentence(result: WhatIfResult): String {
    val name = categoryName(result.categoryName)
    val earlier = result.earlier
    return if (earlier != null) pluralStringResource(R.plurals.goal_whatif_earlier, earlier, name, result.share, earlier)
    else pluralStringResource(R.plurals.goal_whatif_reach, result.months, name, result.share, result.months)
}
