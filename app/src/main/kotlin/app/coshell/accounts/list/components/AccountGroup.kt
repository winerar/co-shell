package app.coshell.accounts.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.accounts.list.AccountsUiState
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellExpandIcon
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/** A group of the accounts list: header with count and total (tap folds it), the rows in one card below. */
@Composable
internal fun AccountGroup(group: AccountsUiState.Group, onToggle: () -> Unit, onAccountClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.rowMinHeight - Spacing.s)
                .tactileClickable(Tactile.Row, MaterialTheme.shapes.medium, onClick = onToggle)
                .padding(horizontal = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            val title = group.titleRes?.let { stringResource(it) } ?: group.titleText.orEmpty()
            Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, color = accentInk())
            Text(group.rows.size.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            group.total?.let { Text(it, style = RateTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            CoShellExpandIcon(group.expanded, contentDescription = null)
        }
        CoShellExpandable(group.expanded) {
            CoShellCard(Modifier.fillMaxWidth()) {
                group.rows.forEachIndexed { index, row ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = Spacing.l))
                    AccountRow(row, onAccountClick)
                }
            }
        }
    }
}
