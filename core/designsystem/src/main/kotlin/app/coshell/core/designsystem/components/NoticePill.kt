package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A small, quiet notice that floats over the bottom bar (design-system.md §«Компоненты»): a neon dot
 * icon, one short line and a «×». Used for "a new version is available"; it never blocks the screen.
 * Rises from below with the spatial spring and pops in slightly, like the tab pill.
 */
@Composable
fun CoShellNoticePill(
    visible: Boolean,
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = visible, modifier = modifier, enter = PillIn, exit = PillOut) {
        val scheme = MaterialTheme.colorScheme
        Row(
            Modifier
                .tactileClickable(Tactile.Control, PillShape, role = Role.Button, onClick = onClick)
                .clip(PillShape)
                .background(scheme.surfaceContainerHigh)
                .border(Sizes.hairline, scheme.primary.copy(alpha = 0.5f), PillShape)
                .heightIn(min = Sizes.pillHeight)
                .padding(start = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Box(
                Modifier.size(Sizes.badgeSmall).clip(PillShape).background(scheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(Sizes.tileIcon))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CoShellIconButton(onClick = onDismiss) {
                Icon(
                    painterResource(app.coshell.core.designsystem.R.drawable.ic_close),
                    contentDescription = dismissLabel,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(Sizes.tileIcon),
                )
            }
        }
    }
}

private val PillIn = slideInVertically(Motion.Placement) { it } + fadeIn(Motion.effects()) +
    scaleIn(Motion.bouncy(), initialScale = 0.8f)
private val PillOut = slideOutVertically(Motion.Placement) { it } + fadeOut(Motion.effectsFast()) +
    scaleOut(Motion.effectsFast(), targetScale = 0.9f)
