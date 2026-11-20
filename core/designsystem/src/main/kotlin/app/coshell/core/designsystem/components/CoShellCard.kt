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
import app.coshell.core.designsystem.theme.LocalDarkTheme
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette

/**
 * What a [CoShellCard] may look like — a closed set (owner 2026-11-20): cards are only of the theme's two accents, the hero
 * apart; [Warning] and [Danger] are the one exception, for states that must catch the eye (a failing sync, entry closed
 * until the update, rates missing, a limit running out or overspent, wiping the data). Where they may appear — `CardToneRuleTest`.
 */
enum class CardTone {
    /** The usual card: a wash of the primary (`surface`) and a thin `outlineVariant` border. */
    Primary,

    /** A wash of the second accent (`secondary`) with a border of it: a block that stands next to a primary one. */
    Secondary,

    /** The one main block of a screen: the palette's gradient without a border (`LocalHeroPalette`, #62, #249). */
    Hero,

    /** Needs attention: a wash and a border of the warning colour (`LocalStatusPalette.warning`). */
    Warning,

    /** Something is lost or over: a wash and a border of the error colour (`colorScheme.error`, the palette's expense). */
    Danger,
}

/** The colour of a washed [tone]'s wash, border and heading; `null` — [CardTone.Primary] and [CardTone.Hero] have none. */
@Composable
internal fun cardToneColor(tone: CardTone): Color? = when (tone) {
    CardTone.Primary, CardTone.Hero -> null
    CardTone.Secondary -> MaterialTheme.colorScheme.secondary
    CardTone.Warning -> LocalStatusPalette.current.warning
    CardTone.Danger -> MaterialTheme.colorScheme.error
}

/**
 * A block on the screen (owner's choice, design-questions §4): rounded, no shadow, of one of the [CardTone]s — no other
 * colour can be given (owner 2026-11-20). Inside the [CardTone.Hero] gradient `onSurface`, `onSurfaceVariant` and the bar
 * track (`surfaceContainerHighest`) follow the gradient, so ordinary text, hints and `CoShellBullet` just work; the main
 * amount takes `LocalHeroPalette.amount`. [onClick] makes the whole card a tap target with the `Tactile.Surface` motion.
 * The card follows its content's height smoothly (issue #64): rows and lines inside may come and go.
 */
@Composable
fun CoShellCard(
    modifier: Modifier = Modifier,
    tone: CardTone = CardTone.Primary,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val hero = LocalHeroPalette.current
    val accent = tone == CardTone.Hero
    val fill = if (accent) hero.brush else null
    val tint = cardToneColor(tone)
    val border = when {
        fill != null -> BorderStroke(0.dp, Color.Transparent)
        tint != null -> BorderStroke(1.dp, tint.copy(alpha = TINT_BORDER_ALPHA))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    val colors = if (fill != null) {
        CardDefaults.outlinedCardColors(containerColor = Color.Transparent, contentColor = hero.content)
    } else {
        CardDefaults.outlinedCardColors(containerColor = tint?.copy(alpha = TINT_ALPHA)?.compositeOver(MaterialTheme.colorScheme.surface) ?: MaterialTheme.colorScheme.surface)
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

/** Strength of a washed [CardTone]: enough to tell the card apart, text keeps its contrast (`NonTextContrastTest`). */
private const val TINT_ALPHA = 0.12f

/** Opacity of a washed [CardTone]'s border. */
private const val TINT_BORDER_ALPHA = 0.55f

/** Inside the hero card: text, hints, the bar track, overspent and warning take the gradient's colours (`HeroPalette`). */
@Composable
private fun HeroColors(content: @Composable () -> Unit) {
    val hero = LocalHeroPalette.current
    val scheme = MaterialTheme.colorScheme
    val dark = LocalDarkTheme.current
    val inside = remember(scheme, hero, dark) {
        scheme.copy(
            onSurface = hero.content,
            onSurfaceVariant = hero.hint,
            surfaceContainerHighest = hero.track,
            // Accent ink inside the pastel light hero is its own ink: the primary's deep shade has no contrast on a primary gradient.
            primaryFixedDim = if (dark) scheme.primaryFixedDim else hero.content,
        )
    }
    CompositionLocalProvider(
        LocalAmountPalette provides LocalAmountPalette.current.copy(expense = hero.expense),
        LocalStatusPalette provides LocalStatusPalette.current.copy(warning = hero.warning),
    ) {
        MaterialTheme(colorScheme = inside, content = content)
    }
}
