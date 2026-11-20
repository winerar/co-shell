package app.coshell.core.designsystem.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.theme.contrastRatio
import app.coshell.core.designsystem.tokens.Sizes

/**
 * An account's badge: a rounded square filled with its colour and two letters of its name (or the bank's
 * initials when the colour is a [BankColors] preset). Ink is dark or white — whichever reads on the fill.
 * No colour → neutral container. Used in the accounts list, the account picker and the colour picker.
 */
@Composable
fun AccountBadge(name: String, color: Long?, modifier: Modifier = Modifier, size: Dp = Sizes.badge, initials: Boolean = true) {
    val fill = color?.let { categoryColor(it) } ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val ink = if (color == null) MaterialTheme.colorScheme.onSurfaceVariant else inkOn(fill)
    val shape = RoundedCornerShape(size * 0.3f)
    val ring = color?.let { CategoryPalette.ring(fill, MaterialTheme.colorScheme.surface) }
    Box(
        modifier = modifier.size(size).background(fill, shape)
            .then(if (ring != null) Modifier.border(Sizes.hairline * 1.5f, ring, shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (initials) Text(
            BankColors.byColor(color)?.initials ?: accountInitials(name),
            color = ink,
            style = if (size < Sizes.badge) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

/**
 * Ink on a coloured [fill] (owner 2026-11-20): dark or white — whichever has more contrast with the fill. The 24 palette
 * colours all read ≥ 4.6:1 with the dark ink (#105), so they keep it; a bank or imported colour may get white.
 */
fun inkOn(fill: Color): Color = if (contrastRatio(DarkInk, fill) >= contrastRatio(Color.White, fill)) DarkInk else Color.White

/**
 * [preferred] text/icon colour on [fill] when it reads (≥ 4.5:1); otherwise flipped to the dark ink or white ([inkOn]).
 * For components that take an ink from the caller (a status-coloured button).
 */
fun readableInk(fill: Color, preferred: Color): Color =
    if (contrastRatio(preferred, fill.compositeOver(Color.White)) >= READABLE_INK) preferred else inkOn(fill)

/** "Galicia" → "GA", "Mercado Pago" → "MP", "" → "·". */
fun accountInitials(name: String): String {
    val words = name.split(' ', '-', '·').filter { w -> w.any { it.isLetterOrDigit() } }
    return when {
        words.isEmpty() -> "·"
        words.size == 1 -> words[0].filter { it.isLetterOrDigit() }.take(2).uppercase()
        else -> "${words[0].first { it.isLetterOrDigit() }}${words[1].first { it.isLetterOrDigit() }}".uppercase()
    }
}

private const val READABLE_INK = 4.5f
private val DarkInk = Color(0xFF0B0B0B) // INK of scripts/agent/themes_1_0.py
