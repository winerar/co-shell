package app.coshell.ui.top

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes

/**
 * The header's bottom hairline: fades in once the content has moved under the header ([scrolled]) and out at
 * the top. Drawn over the bar's bottom edge, so the header keeps its height and nothing below it shifts.
 * One look for tab and inner-screen headers (header polls А and #394 Б, 2026-11-15).
 */
@Composable
internal fun Modifier.scrollHairline(scrolled: Boolean): Modifier {
    val color = MaterialTheme.colorScheme.outlineVariant
    val alpha by animateFloatAsState(if (scrolled) 1f else 0f, Motion.effects(), label = "hairline")
    return drawWithContent {
        drawContent()
        val width = Sizes.hairline.toPx()
        val y = size.height - width / 2
        drawLine(color.copy(alpha = alpha), Offset(0f, y), Offset(size.width, y), width)
    }
}
