package app.coshell.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.amount.fitAmountAutoSize
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.LocalCompactLists
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * One account as a list row — the same everywhere an account is listed (owner 2026-11-20: one look for the same data):
 * «Счета» (`accounts/list/components/AccountRow`) and the account picker of the entry sheet (`AccountPicker`).
 * Badge in the account's colour, name, [supporting] (type…), balance and its value in the main currency; [selected] adds a ✓.
 */
@Composable
fun AccountLine(
    name: String,
    color: Long?,
    supporting: String,
    balance: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isNegative: Boolean = false,
    converted: String? = null,
    /** «не в общем балансе»: the subtitle in the attention colour. */
    warn: Boolean = false,
    selected: Boolean = false,
    /** «Только у меня» (#558): a lock after the name. */
    locked: Boolean = false,
    lockedDescription: String? = null,
) {
    val palette = LocalAmountPalette.current
    CoShellListRow(
        compact = LocalCompactLists.current,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (locked) {
                    Icon(
                        painterResource(DesignR.drawable.ic_lock),
                        contentDescription = lockedDescription,
                        modifier = Modifier.size(Sizes.tileIcon),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        modifier = modifier,
        supporting = {
            Text(
                supporting,
                color = if (warn) LocalStatusPalette.current.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leading = { AccountBadge(name, color) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Column(horizontalAlignment = Alignment.End) {
                    if (balance.isNotEmpty()) {
                        AmountText(
                            text = balance,
                            style = AmountTextStyle,
                            color = if (isNegative) palette.expense else MaterialTheme.colorScheme.onSurface,
                            minFontSize = MinAmountSize,
                        )
                    }
                    converted?.let {
                        Text(
                            shownAmount(it),
                            style = RateTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            autoSize = fitAmountAutoSize(min = MinAmountSize, max = RateTextStyle.fontSize),
                        )
                    }
                }
                if (selected) {
                    Icon(painterResource(DesignR.drawable.ic_check), stringResource(R.string.account_selected), tint = accentInk())
                }
            }
        },
        onClick = onClick,
    )
}

/** Title of an account group, as on «Счета»: the name in capitals (accent, or grey for [muted]) and the count. */
@Composable
fun AccountGroupTitle(title: String, count: Int, modifier: Modifier = Modifier, muted: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else accentInk(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Text(count.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A long balance shrinks down to this instead of losing its currency code (#431), as in the operations list. */
private val MinAmountSize = 9.sp
