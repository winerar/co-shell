package app.coshell.core.designsystem.category

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.theme.LocalDarkTheme

/**
 * A category's icon in a circle filled with its colour, dark glyph (1.0, #105; board Palette24). Used everywhere a category is
 * shown: category list, transaction list, pickers. Missing colour → neutral grey.
 */
@Composable
fun CategoryBadge(icon: String?, color: Long?, modifier: Modifier = Modifier, size: Dp = Sizes.badge) {
    IconBadge(CategoryIcons.drawableFor(icon), color?.let { categoryColor(it) }, modifier, size)
}

/** On-screen colour of a stored category colour in the current theme ([CategoryPalette.display]). */
@Composable
fun categoryColor(stored: Long): Color = Color(CategoryPalette.display(stored, LocalDarkTheme.current))

/**
 * Same circle for any drawable, e.g. transfers in the transaction list. Filled with [color] and a dark glyph (1.0, #105:
 * the 24 colours read the same in both themes); a pale fill gets a thin ring of its deeper shade ([CategoryPalette.ring]).
 * `null` [color] = neutral container with a grey glyph.
 */
@Composable
fun IconBadge(@DrawableRes iconRes: Int, color: Color?, modifier: Modifier = Modifier, size: Dp = Sizes.badge) {
    val scheme = MaterialTheme.colorScheme
    val fill = color ?: scheme.surfaceContainerHighest
    val ring = color?.let { CategoryPalette.ring(it, scheme.surface) }
    Box(
        modifier = modifier
            .size(size)
            .background(fill, CircleShape)
            .then(if (ring != null) Modifier.border(Sizes.hairline * RING_WIDTH, ring, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (color == null) scheme.onSurfaceVariant else inkOn(fill),
            modifier = Modifier.size(size * 0.55f),
        )
    }
}

/** Ring of a pale badge: 1.5 hairlines. */
private const val RING_WIDTH = 1.5f

/**
 * A palette colour as a circle of [size] (1.0, #105): the picker dots, the colour strips. A pale fill gets the same thin
 * ring as [IconBadge]. [content] sits on it in the dark ink (a check mark of the chosen colour).
 */
@Composable
fun ColorFill(color: Long, modifier: Modifier = Modifier, size: Dp = Sizes.badge, content: @Composable () -> Unit = {}) {
    val fill = categoryColor(color)
    val ring = CategoryPalette.ring(fill, MaterialTheme.colorScheme.surface)
    Box(
        modifier = modifier
            .size(size)
            .background(fill, CircleShape)
            .then(if (ring != null) Modifier.border(Sizes.hairline * RING_WIDTH, ring, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) { content() }
}
