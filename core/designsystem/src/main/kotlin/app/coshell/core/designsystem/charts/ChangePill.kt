package app.coshell.core.designsystem.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A small pill of a change next to an amount (#238, board `Tr238MarkA`): «↑ 45%», «↓ 33%», «новое» — the [text] in
 * [color] on a faint wash of it. Drawn only: the row it sits in says the change to TalkBack in words.
 */
@Composable
fun CoShellChangePill(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        style = RateTextStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize, fontWeight = FontWeight.SemiBold),
        color = color,
        maxLines = 1,
        modifier = modifier
            .clearAndSetSemantics {}
            .background(color.copy(alpha = WASH_ALPHA), CircleShape)
            .heightIn(min = PILL_HEIGHT)
            .padding(horizontal = Spacing.s - 1.dp, vertical = Spacing.xxs),
    )
}

private val PILL_HEIGHT = 20.dp
private const val WASH_ALPHA = 0.14f
