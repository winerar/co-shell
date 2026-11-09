package app.coshell.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactile
import app.coshell.core.designsystem.theme.LocalHeroPalette

/**
 * A block on the screen (owner's choice, design-questions §4): background of the screen, thin
 * `outlineVariant` border, 16 dp corners, no shadow. [accent] — the main block of a screen (total on
 * «Обзор»): in the light theme a neon gradient fill without a border (`LocalHeroPalette`, issue #62), in the
 * dark theme the border takes the primary neon. Inside it, take text colors from `LocalHeroPalette`.
 * [tint] — a user-chosen colour (an account's): a soft wash of it over the background and a border of it. [onClick] makes the whole card a tap target with the `Tactile.Surface` motion.
 */
@Composable
fun CoShellCard(
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    tint: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val hero = LocalHeroPalette.current
    val fill = if (accent) hero.brush else null
    val border = when {
        fill != null -> BorderStroke(0.dp, Color.Transparent)
        accent -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        tint != null -> BorderStroke(1.dp, tint.copy(alpha = 0.55f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    val colors = if (fill != null) {
        CardDefaults.outlinedCardColors(containerColor = Color.Transparent, contentColor = hero.content)
    } else {
        val surface = MaterialTheme.colorScheme.surface
        CardDefaults.outlinedCardColors(containerColor = tint?.copy(alpha = TINT_ALPHA)?.compositeOver(surface) ?: surface)
    }
    val filled = if (fill != null) Modifier.background(fill, shape) else Modifier
    if (onClick != null) {
        val source = remember { MutableInteractionSource() }
        NoRipple {
            OutlinedCard(
                onClick = onClick,
                modifier = modifier.tactile(source, Tactile.Surface, shape).then(filled),
                shape = shape,
                colors = colors,
                border = border,
                interactionSource = source,
                content = content,
            )
        }
    } else {
        OutlinedCard(modifier = modifier.then(filled), shape = shape, colors = colors, border = border, content = content)
    }
}

/** Strength of a [CoShellCard] tint: enough to tell accounts apart, text keeps its contrast. */
private const val TINT_ALPHA = 0.12f
