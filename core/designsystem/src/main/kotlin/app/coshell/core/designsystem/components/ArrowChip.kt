package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.tokens.Sizes

/**
 * The round arrow between two things that change into one another («Сейчас → Новая», poll 2026-11-18): the neon
 * with dark ink and the thin edge (`fillEdge`) where the neon melts into the page. Drawn only, the tiles say the rest in words.
 */
@Composable
fun CoShellArrowChip(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier
            .size(Sizes.arrowChip)
            .background(colors.primary, CircleShape)
            .fillEdge(colors.primary, CircleShape, colors.background)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(Sizes.tileIcon))
    }
}
