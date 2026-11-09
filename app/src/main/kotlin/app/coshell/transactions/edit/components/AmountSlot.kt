package app.coshell.transactions.edit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.RollingAmountText
import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.AmountHeroStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditUiState.AmountError

/**
 * An amount typed on the keypad: digits roll as they change, the line under it lights up while the keypad
 * types here ([active]); a tap makes it the keypad's target. [big] — the main amount of the sheet.
 */
@Composable
internal fun AmountSlot(
    label: String?,
    text: String,
    currency: String?,
    active: Boolean,
    error: AmountError?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    big: Boolean = false,
    hint: String? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val line by animateColorAsState(
        when {
            error != null -> MaterialTheme.colorScheme.error
            active -> accentInk()
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        Motion.effects(),
        label = "amountLine",
    )
    Column(
        modifier = modifier.fillMaxWidth().tactileClickable(Tactile.Surface, MaterialTheme.shapes.medium, onClick = onClick),
        horizontalAlignment = if (big) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            RollingAmountText(
                AmountKeys.display(AmountKeys.normalize(text)),
                style = if (big) AmountHeroStyle else AmountTitleStyle,
                color = color,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (currency != null) {
                Text(
                    currency,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.xs),
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(Sizes.selectionRing).background(line, MaterialTheme.shapes.small))
        val message = error?.let { stringResource(it.messageRes) } ?: hint
        if (message != null) {
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal val AmountError.messageRes: Int
    get() = when (this) {
        AmountError.REQUIRED -> R.string.transaction_amount_required
        AmountError.INVALID -> R.string.transaction_amount_invalid
        AmountError.TOO_MANY_FRACTION_DIGITS -> R.string.transaction_amount_fraction
        AmountError.NOT_POSITIVE -> R.string.transaction_amount_not_positive
    }
