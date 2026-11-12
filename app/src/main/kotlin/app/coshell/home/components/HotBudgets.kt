package app.coshell.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.list.BudgetsUiState
import app.coshell.budgets.list.components.EnvelopeRow
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.tokens.Spacing

/**
 * «Горит в бюджете» (board `BudHome`): the envelopes over the limit or ahead of pace, the same rows as on «Бюджеты»;
 * a tap opens the envelope. The caller shows it only when there are some.
 */
@Composable
internal fun HotBudgets(envelopes: List<BudgetsUiState.Envelope>, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        CoShellSectionLabel(stringResource(R.string.home_budget_hot), Modifier.padding(start = Spacing.xs))
        CoShellCard(Modifier.fillMaxWidth().padding(top = Spacing.s)) {
            envelopes.forEachIndexed { index, envelope ->
                key(envelope.categoryId) {
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = Spacing.l))
                    }
                    EnvelopeRow(envelope, onClick = { onOpen(envelope.categoryId) })
                }
            }
        }
    }
}
