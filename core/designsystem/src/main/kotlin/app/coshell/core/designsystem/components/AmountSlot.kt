package app.coshell.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.amount.RollingAmountText
import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.AmountSlotStyle
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Radii
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import app.coshell.core.designsystem.amount.fitAmountAutoSize

/**
 * An amount typed on the keypad (board `CompNew`, «Сумма», poll #367 А): a field of the form's look — frame `outline`,
 * radius 16 — with the optional [label] on top, the number rolling in the mono font ([main]: 28 sp, otherwise 22) and
 * the currency as a pill at the end ([currency] plain, or [trailing] for a pill that does something). The frame turns
 * to the ink while the keypad types here ([active]) and red on [isError]; [message] (error or hint) sits under it.
 * [compact] (#818, «Покупка 13 300 ARS ▾» of the entry sheet): one pill 40 dp — the label inline before the number,
 * no [message] (the caller puts the rate or the error next to it); the width follows the content.
 */
@Composable
fun CoShellAmountSlot(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    currency: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    active: Boolean = false,
    isError: Boolean = false,
    message: String? = null,
    main: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface,
    compact: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    if (compact) {
        CompactSlot(text, onClick, modifier, label, currency, trailing, active, isError, color)
        return
    }
    val frame by animateColorAsState(
        when {
            isError -> scheme.error
            active -> scheme.onSurface
            else -> scheme.outline
        },
        Motion.effects(),
        label = "amountSlotFrame",
    )
    val shape = RoundedCornerShape(Radii.medium)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = if (main) Sizes.amountSlotMain else Sizes.amountSlot)
                .tactileClickable(Tactile.Surface, shape, role = Role.Button, onClick = onClick)
                .background(scheme.surfaceContainer, shape)
                .border(if (active || isError) Sizes.selectionRing else Sizes.hairline, frame, shape)
                .padding(horizontal = Spacing.l, vertical = Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                if (label != null) Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isError) scheme.error else scheme.onSurfaceVariant,
                    maxLines = 1,
                    // The label shrinks instead of breaking into letters next to a wide pill (320 dp, font ×2).
                    autoSize = fitAmountAutoSize(min = 8.sp, max = MaterialTheme.typography.labelMedium.fontSize),
                )
                RollingAmountText(
                    AmountKeys.display(AmountKeys.normalize(text)),
                    style = if (main) AmountSlotStyle else AmountTitleStyle,
                    color = color,
                )
            }
            if (trailing != null) trailing()
            else if (currency != null) CurrencyPill(currency)
        }
        if (message != null) {
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) scheme.error else scheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.l),
            )
        }
    }
}

/** [CoShellAmountSlot] in one pill 40 dp: label · number · currency (#818). */
@Composable
private fun CompactSlot(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    label: String?,
    currency: String?,
    trailing: (@Composable () -> Unit)?,
    active: Boolean,
    isError: Boolean,
    color: Color,
) {
    val scheme = MaterialTheme.colorScheme
    val frame by animateColorAsState(
        when {
            isError -> scheme.error
            active -> scheme.onSurface
            else -> scheme.outline
        },
        Motion.effects(),
        label = "amountSlotFrame",
    )
    BoxWithConstraints(modifier) {
        // The label fits whole or not at all: on a narrow screen with a big font (320 dp, ×1,3) the number keeps the room.
        val withLabel = label != null && maxWidth >= CompactLabelRoom * LocalDensity.current.fontScale
        Row(
            Modifier
                .heightIn(min = Sizes.detailPill)
                .tactileClickable(Tactile.Surface, PillShape, role = Role.Button, onClick = onClick)
                .background(scheme.surfaceContainer, PillShape)
                .border(if (active || isError) Sizes.selectionRing else Sizes.hairline, frame, PillShape)
                .padding(start = Spacing.m, end = Spacing.xxs, top = Spacing.xxs, bottom = Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            if (withLabel && label != null) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = if (isError) scheme.error else scheme.onSurfaceVariant, maxLines = 1, softWrap = false)
            }
            // A long number shrinks (RollingAmountText fits its width) instead of pushing the currency out.
            RollingAmountText(AmountKeys.display(AmountKeys.normalize(text)), style = AmountTextStyle, color = color, modifier = Modifier.weight(1f, fill = false))
            if (trailing != null) trailing()
            else if (currency != null) CurrencyPill(currency)
        }
    }
}

private val CompactLabelRoom = 180.dp

/** The currency of an amount: a quiet pill, not pressable. */
@Composable
private fun CurrencyPill(code: String) {
    Text(
        code,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        modifier = Modifier
            .heightIn(min = Sizes.chipHeight)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, PillShape)
            .padding(horizontal = Spacing.m, vertical = Spacing.s),
    )
}
