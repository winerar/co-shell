package app.coshell.accounts.edit.components

import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.accounts.labelRes
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.category.inkOn
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Spacing

/**
 * The account as a card at the top of its editor (mockup 10): its colour, name, type and starting balance
 * change live while the form is filled. Out of the total balance — the card fades. Compact (#290): the form fits.
 */
@Composable
internal fun AccountPreviewCard(state: AccountEditUiState, modifier: Modifier = Modifier) {
    val target = state.color?.let { categoryColor(it) } ?: MaterialTheme.colorScheme.primary
    val fill by animateColorAsState(target, Motion.effects(), label = "fill")
    val ink by animateColorAsState(inkOn(target), Motion.effects(), label = "ink")
    val alpha by animateFloatAsState(if (state.includedInTotal) 1f else FADED, Motion.effects(), label = "alpha")
    Column(
        modifier
            .fillMaxWidth()
            .alpha(alpha)
            .background(fill, MaterialTheme.shapes.extraLarge)
            .padding(horizontal = Spacing.l, vertical = Spacing.m),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(
                state.name.ifBlank { stringResource(R.string.account_edit_title_new) },
                color = ink,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(stringResource(state.type.labelRes), color = ink, style = MaterialTheme.typography.labelLarge)
        }
        Text(
            listOf(state.initialBalance.ifBlank { "0" }, state.currency).filter { it.isNotBlank() }.joinToString(" "),
            color = ink,
            style = AmountTitleStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = Spacing.s),
        )
    }
}

/** A card of an account outside the total balance is shown half-transparent. */
private const val FADED = 0.55f
