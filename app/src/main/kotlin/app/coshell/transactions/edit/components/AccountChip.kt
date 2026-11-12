package app.coshell.transactions.edit.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption

/** The chosen account at the top of step 2: badge, name, balance and ▾; a tap goes back to the picker. */
@Composable
internal fun AccountChip(account: AccountOption, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .tactileClickable(Tactile.Control, PillShape, onClick = onClick)
            .border(Sizes.selectionRing / 2, MaterialTheme.colorScheme.outlineVariant, PillShape)
            .padding(start = Spacing.xs, end = Spacing.l, top = Spacing.xs, bottom = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        AccountBadge(account.name, account.color, size = Sizes.badgeSmall)
        Column(Modifier.widthIn(max = Sizes.tileMinHeight)) {
            Text(account.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                account.balance.ifEmpty { account.currency },
                style = RateTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painterResource(DesignR.drawable.ic_keyboard_arrow_down),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Sizes.searchIcon),
        )
    }
}
