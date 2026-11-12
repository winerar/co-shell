package app.coshell.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.smoothResize
import app.coshell.core.designsystem.motion.tactile
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette

/**
 * A block on the screen (owner's choice, design-questions §4): background of the screen, thin
 * `outlineVariant` border, 16 dp corners, no shadow. [accent] — the one main block of a screen (total on «Обзор»,
 * the month on «Бюджеты», an envelope's head): a gradient of the palette without a border in both themes
 * (`LocalHeroPalette`, issues #62, #249). Inside it `onSurface`, `onSurfaceVariant` and the bar track
 * (`surfaceContainerHighest`) follow the gradient, so ordinary text, hints and `CoShellBullet` just work;
 * the main amount takes `LocalHeroPalette.amount`.
 * [tint] — a user-chosen colour (an account's): a soft wash of it over the background and a border of it. [onClick] makes the whole card a tap target with the `Tactile.Surface` motion.
 * The card follows its content's height smoothly (issue #64): rows and lines inside may come and go.
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
        tint != null -> BorderStroke(1.dp, tint.copy(alpha = 0.55f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    val colors = if (fill != null) {
        CardDefaults.outlinedCardColors(containerColor = Color.Transparent, contentColor = hero.content)
    } else {
        CardDefaults.outlinedCardColors(containerColor = tint?.let { tintedCardFill(it) } ?: MaterialTheme.colorScheme.surface)
    }
    val filled = if (fill != null) Modifier.background(fill, shape) else Modifier
    val body: @Composable ColumnScope.() -> Unit = if (accent) ({ HeroColors { content() } }) else content
    if (onClick != null) {
        val source = remember { MutableInteractionSource() }
        NoRipple {
            OutlinedCard(
                onClick = onClick,
                modifier = modifier.tactile(source, Tactile.Surface, shape).then(filled).smoothResize(),
                shape = shape,
                colors = colors,
                border = border,
                interactionSource = source,
                content = body,
            )
        }
    } else {
        OutlinedCard(modifier = modifier.then(filled).smoothResize(), shape = shape, colors = colors, border = border, content = body)
    }
}

/** Strength of a [CoShellCard] tint: enough to tell accounts apart, text keeps its contrast. */
/** The fill of a [CoShellCard] with `tint` — for things that must blend into it (a ring around an avatar). */
@Composable
fun tintedCardFill(tint: Color): Color = tint.copy(alpha = TINT_ALPHA).compositeOver(MaterialTheme.colorScheme.surface)

private const val TINT_ALPHA = 0.12f

/** Inside the hero card: text, hints, the bar track, overspent and warning take the gradient's colours (`HeroPalette`). */
@Composable
private fun HeroColors(content: @Composable () -> Unit) {
    val hero = LocalHeroPalette.current
    val scheme = MaterialTheme.colorScheme
    val inside = remember(scheme, hero) {
        scheme.copy(onSurface = hero.content, onSurfaceVariant = hero.hint, surfaceContainerHighest = hero.track)
    }
    CompositionLocalProvider(
        LocalAmountPalette provides LocalAmountPalette.current.copy(expense = hero.expense),
        LocalStatusPalette provides LocalStatusPalette.current.copy(warning = hero.warning),
    ) {
        MaterialTheme(colorScheme = inside, content = content)
    }
}
