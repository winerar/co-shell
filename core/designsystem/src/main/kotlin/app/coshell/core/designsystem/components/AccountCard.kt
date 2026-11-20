package app.coshell.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import app.coshell.core.designsystem.amount.fitAmountAutoSize
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.category.inkOn
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * The card of an account (board `CompNew`, «Превью карточки счёта», #367 А): filled with the bank's [color] (the theme's
 * accent without one), the ink by contrast; name and [kind] on top, [balance] under. The colour changes smoothly while
 * the form is filled; [faded] — an account outside the total is half-transparent.
 */
@Composable
fun CoShellAccountCard(
    name: String,
    kind: String,
    balance: String,
    color: Long?,
    modifier: Modifier = Modifier,
    faded: Boolean = false,
) {
    val target = color?.let { categoryColor(it) } ?: MaterialTheme.colorScheme.primary
    val fill by animateColorAsState(target, Motion.effects(), label = "accountCardFill")
    val ink by animateColorAsState(inkOn(target), Motion.effects(), label = "accountCardInk")
    val alpha by animateFloatAsState(if (faded) FADED else 1f, Motion.effects(), label = "accountCardAlpha")
    Column(
        modifier
            .fillMaxWidth()
            .alpha(alpha)
            .background(fill, MaterialTheme.shapes.large)
            .then(accountEdgeColor(fill, MaterialTheme.colorScheme.background)?.let { Modifier.border(Sizes.hairline, it, MaterialTheme.shapes.large) } ?: Modifier)
            .padding(horizontal = Spacing.l, vertical = Spacing.m),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(name, color = ink, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text(kind, color = ink, style = MaterialTheme.typography.labelLarge)
        }
        Text(
            balance,
            color = ink,
            style = AmountTitleStyle,
            maxLines = 1,
            // The typed balance shrinks to fit — a cut amount shows a wrong number (#378).
            autoSize = fitAmountAutoSize(min = 10.sp, max = AmountTitleStyle.fontSize),
            modifier = Modifier.padding(top = Spacing.s),
        )
    }
}

private const val FADED = 0.55f
