package app.coshell.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.LocalDarkTheme
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.theme.themeColorsFor
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One theme to pick in «Внешний вид» (board Fb271A, #271): a rounded square in the palette's own background
 * (of the current light/dark side) with a big dot of its primary and a small dot of its income, the [label] under it.
 * Selected — a ring with a gap that fades in, and a pop. Laid out four in a row.
 */
@Composable
fun CoShellPaletteSwatch(
    palette: CoShellPalette,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = LocalDarkTheme.current
    val colors = remember(palette, dark) { themeColorsFor(palette, dark) }
    val shape = MaterialTheme.shapes.large
    val ring by animateColorAsState(if (selected) accentInk() else Color.Transparent, Motion.effects(), label = "ring")
    Column(
        modifier
            .tactileClickable(Tactile.Control, shape, role = Role.RadioButton, onClick = onClick)
            .selectionPop(selected),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(Modifier.border(Sizes.selectionRing, ring, shape).padding(Spacing.xxs)) {
            Box(
                Modifier
                    .size(TILE)
                    .background(colors.scheme.background, shape)
                    .border(Sizes.hairline, MaterialTheme.colorScheme.outlineVariant, shape),
            ) {
                Box(Modifier.align(Alignment.TopStart).padding(DOT_INSET).size(DOT).background(colors.scheme.primary, CircleShape))
                Box(
                    Modifier.align(Alignment.BottomEnd).padding(SMALL_DOT_INSET).size(SMALL_DOT)
                        .background(colors.amounts.income, CircleShape),
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val TILE = 56.dp
private val DOT = 24.dp
private val DOT_INSET = 10.dp
private val SMALL_DOT = 12.dp
private val SMALL_DOT_INSET = 9.dp
