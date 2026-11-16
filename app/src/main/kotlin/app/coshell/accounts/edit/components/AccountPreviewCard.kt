package app.coshell.accounts.edit.components

import androidx.compose.runtime.getValue
import app.coshell.core.designsystem.components.CoShellAccountCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.accounts.labelRes

/**
 * The account as a card at the top of its editor (mockup 10): its colour, name, type and starting balance
 * change live while the form is filled. Out of the total balance — the card fades. Compact (#290): the form fits.
 */
@Composable
internal fun AccountPreviewCard(state: AccountEditUiState, modifier: Modifier = Modifier) {
    CoShellAccountCard(
        name = state.name.ifBlank { stringResource(R.string.account_edit_title_new) },
        kind = stringResource(state.type.labelRes),
        balance = listOf(state.initialBalance.ifBlank { "0" }, state.currency).filter { it.isNotBlank() }.joinToString(" "),
        color = state.color,
        modifier = modifier,
        faded = !state.includedInTotal,
    )
}
