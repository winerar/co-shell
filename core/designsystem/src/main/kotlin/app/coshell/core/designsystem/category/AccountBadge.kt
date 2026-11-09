package app.coshell.core.designsystem.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.tokens.Sizes

/**
 * An account's badge: a rounded square filled with its colour and two letters of its name (or the bank's
 * initials when the colour is a [BankColors] preset). Ink is dark or white — whichever reads on the fill.
 * No colour → neutral container. Used in the accounts list, the account picker and the colour picker.
 */
@Composable
fun AccountBadge(name: String, color: Long?, modifier: Modifier = Modifier, size: Dp = Sizes.badge) {
    val fill = color?.let { categoryColor(it) } ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val ink = if (color == null) MaterialTheme.colorScheme.onSurfaceVariant else inkOn(fill)
    Box(
        modifier = modifier.size(size).background(fill, RoundedCornerShape(size * 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            BankColors.byColor(color)?.initials ?: accountInitials(name),
            color = ink,
            style = if (size < Sizes.badge) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

/** Dark ink on light fills, white on dark ones. */
fun inkOn(fill: Color): Color = if (fill.luminance() > INK_THRESHOLD) DarkInk else Color.White

/** "Galicia" → "GA", "Mercado Pago" → "MP", "" → "·". */
fun accountInitials(name: String): String {
    val words = name.split(' ', '-', '·').filter { w -> w.any { it.isLetterOrDigit() } }
    return when {
        words.isEmpty() -> "·"
        words.size == 1 -> words[0].filter { it.isLetterOrDigit() }.take(2).uppercase()
        else -> "${words[0].first { it.isLetterOrDigit() }}${words[1].first { it.isLetterOrDigit() }}".uppercase()
    }
}

private const val INK_THRESHOLD = 0.4f
private val DarkInk = Color(0xFF111611)
