package app.coshell.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.LocalDarkTheme
import app.coshell.core.designsystem.theme.ThemeColors
import app.coshell.core.designsystem.theme.themeColorsFor
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One theme to pick in «Внешний вид» (owner's choice 2026-11-16, picker «А»): a small screen of the theme — its background
 * (of the current light/dark side), the hero gradient, the accent and income dots on two lines, the primary button —
 * and the [label] under it. Selected — an ink ring with a check and a pop. Laid out three in a row.
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
    val ink = MaterialTheme.colorScheme.onSurface
    val ring by animateColorAsState(if (selected) ink else Color.Transparent, Motion.effects(), label = "ring")
    Column(
        modifier
            .tactileClickable(Tactile.Control, shape, role = Role.RadioButton, onClick = onClick)
            .selectionPop(selected),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(Modifier.border(Sizes.selectionRing, ring, shape).padding(Spacing.xxs)) {
            MiniScreen(colors, shape)
            if (selected) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(Spacing.xs).size(CHECK).background(ink, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_check), null, Modifier.size(CHECK_ICON), tint = MaterialTheme.colorScheme.surface)
                }
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MiniScreen(colors: ThemeColors, shape: androidx.compose.ui.graphics.Shape) {
    val s = colors.scheme
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(TILE_RATIO)
            .background(s.background, shape)
            .border(Sizes.hairline, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(Modifier.fillMaxWidth().height(HERO).background(Brush.linearGradient(colors.heroStops), MaterialTheme.shapes.small))
        MiniLine(s.secondary, s.outlineVariant)
        MiniLine(colors.amounts.income, s.outlineVariant)
        Spacer(Modifier.weight(1f))
        Box(Modifier.align(Alignment.End).size(width = BUTTON_W, height = BUTTON_H).background(s.primary, MaterialTheme.shapes.small))
    }
}

@Composable
private fun MiniLine(dot: Color, line: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Box(Modifier.size(DOT).background(dot, CircleShape))
        Box(Modifier.weight(1f).height(LINE).background(line, CircleShape))
    }
}

private const val TILE_RATIO = 0.72f
private val HERO = 36.dp
private val DOT = 12.dp
private val LINE = 5.dp
private val BUTTON_W = 30.dp
private val BUTTON_H = 14.dp
private val CHECK = 22.dp
private val CHECK_ICON = 14.dp
