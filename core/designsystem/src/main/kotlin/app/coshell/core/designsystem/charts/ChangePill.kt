package app.coshell.core.designsystem.charts

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import app.coshell.core.designsystem.components.labelCapsule
import app.coshell.core.designsystem.theme.RateTextStyle

/**
 * A small label of a change next to an amount (#238): «↑ 45%», «↓ 33%», «новое». The one label shape (board Style2):
 * a 22 dp capsule tinted with [color]; the colour goes only into a leading arrow, the figures stay in ink (#385).
 * Drawn only: the row it sits in says the change to TalkBack in words.
 */
@Composable
fun CoShellChangePill(text: String, color: Color, modifier: Modifier = Modifier) {
    val arrow = text.firstOrNull()?.takeIf { it in ARROWS }
    Box(modifier.clearAndSetSemantics {}.labelCapsule(color), contentAlignment = Alignment.Center) {
        Text(
            buildAnnotatedString {
                if (arrow != null) {
                    withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append(arrow) }
                    append(text.drop(1))
                } else {
                    append(text)
                }
            },
            style = RateTextStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

private const val ARROWS = "↑↓"
