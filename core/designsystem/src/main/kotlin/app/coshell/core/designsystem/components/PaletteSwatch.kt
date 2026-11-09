package app.coshell.core.designsystem.components

import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
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
 * One palette to pick in «Настройки → Тема»: a small card in the palette's own background with its three
 * key colours (primary, accent, income) and the [label] under it. Selected — a ring that fades in and a pop.
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
        Box(
            Modifier
                .border(Sizes.selectionRing, ring, shape)
                .padding(Spacing.xs)
                .background(colors.scheme.surfaceContainer, shape)
                .padding(Spacing.m),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                listOf(colors.scheme.primary, colors.scheme.secondary, colors.amounts.income).forEach { dot ->
                    Box(Modifier.size(DOT).background(dot, CircleShape))
                }
            }
        }
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

/**
 * A palette as a card for a two-column grid (mockup 15): its three key colours over the palette's own
 * surface, the [label] and a short [hint] under them. Selected — a ring in the palette's primary.
 */
@Composable
fun CoShellPaletteCard(
    palette: CoShellPalette,
    label: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = LocalDarkTheme.current
    val colors = remember(palette, dark) { themeColorsFor(palette, dark) }
    val shape = MaterialTheme.shapes.large
    val ring by animateColorAsState(
        if (selected) colors.scheme.primary else MaterialTheme.colorScheme.outlineVariant,
        Motion.effects(),
        label = "ring",
    )
    Column(
        modifier
            .tactileClickable(Tactile.Control, shape, role = Role.RadioButton, onClick = onClick)
            .selectionPop(selected)
            .border(if (selected) Sizes.selectionRing else Sizes.hairline, ring, shape)
            .background(MaterialTheme.colorScheme.surfaceContainer, shape)
            .padding(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            listOf(colors.scheme.primary, colors.scheme.secondary, colors.amounts.income).forEach { dot ->
                Box(Modifier.size(CARD_DOT).background(dot, CircleShape))
            }
        }
        Column {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(hint, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private val CARD_DOT = 26.dp

/** Colour dots overlap a little, like paint samples. */
private val DOT = 20.dp
