package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption

/** The chosen account at the top of step 2: a choice chip with the badge, the name and ▾; a tap goes back to the picker. */
@Composable
internal fun AccountChip(account: AccountOption, onClick: () -> Unit) {
    CoShellFilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(account.name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = Sizes.tileMinHeight)) },
        leadingIcon = { AccountBadge(account.name, account.color, size = Sizes.chipBadge) },
        trailingIcon = { Icon(painterResource(DesignR.drawable.ic_keyboard_arrow_down), contentDescription = null, modifier = Modifier.size(Sizes.chipIcon)) },
    )
}
