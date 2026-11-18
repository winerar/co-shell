package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAccountRibbon
import app.coshell.core.designsystem.components.RibbonAccount
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.transactions.edit.ribbonAccounts

/**
 * Accounts of the one-step entry sheet (#505, board «Лента счетов»): a ribbon of badges; a transfer has two —
 * «Откуда» and «Куда». «Все ›» opens the full list ([AccountPicker]) and returns here after a pick.
 */
@Composable
internal fun AccountRibbonBlock(state: TransactionEditUiState, actions: TransactionEditActions, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        if (state.isTransfer) RibbonLabel(stringResource(R.string.entry_from))
        Ribbon(state, state.accountId, null, actions.onAccountSelect) { actions.onStepChange(EntryStep.ACCOUNT) }
        if (state.isTransfer) {
            RibbonLabel(stringResource(R.string.entry_to))
            Ribbon(state, state.toAccountId, state.accountId, actions.onToAccountSelect) { actions.onStepChange(EntryStep.TO_ACCOUNT) }
        }
    }
}

@Composable
private fun Ribbon(state: TransactionEditUiState, selectedId: String?, excludeId: String?, onSelect: (String) -> Unit, onAll: () -> Unit) {
    val shown = ribbonAccounts(state.accounts, state.recentAccountIds, selectedId, excludeId)
    CoShellAccountRibbon(
        accounts = shown.map { RibbonAccount(it.id, it.name, it.color) },
        selectedId = selectedId,
        onSelect = onSelect,
        onAll = onAll,
        allLabel = stringResource(R.string.picker_all),
    )
}

@Composable
private fun RibbonLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = Spacing.l),
    )
}
