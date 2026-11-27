package app.coshell.budgets.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.edit.UsualLimit
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.tokens.Spacing

/**
 * «Обычно уходит ~420 000 ARS в месяц · Подставить» under the limit field (#790, poll 2026-11-27 «А», board `Smart790Hint`):
 * the person decides; the pill disappears once the field holds that figure. A [FlowRow]: on a narrow screen the pill goes under the text.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UsualLimitRow(usual: UsualLimit, amount: String, onUse: () -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.Start),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.budget_usual, shownAmount(usual.shown)),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (amount.trim() != usual.editing) {
            CoShellFilterChip(selected = false, onClick = onUse, label = { Text(stringResource(R.string.budget_usual_use)) })
        }
    }
}
