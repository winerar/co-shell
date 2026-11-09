package app.coshell.core.designsystem.category

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.theme.LocalDarkTheme

/**
 * A category's icon in a softly tinted circle of its colour (shade for the current theme). Used everywhere a category is
 * shown: category list, transaction list, pickers. Missing colour → neutral grey.
 */
@Composable
fun CategoryBadge(icon: String?, color: Long?, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    IconBadge(CategoryIcons.drawableFor(icon), color?.let { categoryColor(it) }, modifier, size)
}

/** On-screen colour of a stored category colour in the current theme ([CategoryPalette.display]). */
@Composable
fun categoryColor(stored: Long): Color = Color(CategoryPalette.display(stored, LocalDarkTheme.current))

/** Same circle for any drawable, e.g. transfers in the transaction list. `null` [color] = neutral. */
@Composable
fun IconBadge(@DrawableRes iconRes: Int, color: Color?, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val tint = color ?: MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .size(size)
            .background(tint.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}
