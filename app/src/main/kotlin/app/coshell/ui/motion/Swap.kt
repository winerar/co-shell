package app.coshell.ui.motion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.motion.dataSwap
import app.coshell.core.designsystem.tokens.Motion

/**
 * A block whose data changes in place (owner 2026-11-27: in report cards switching data is smooth): when [target] changes
 * the old content lifts and fades, the new rises in, the size follows on the spring ([dataSwap]). Key it by the data the
 * block shows, not by the whole card, so charts inside a card keep moving their own marks.
 */
@Composable
internal fun <T> Swap(
    target: T,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable (T) -> Unit,
) {
    // The box takes [modifier] (a weight, a full width); AnimatedContent itself is only as wide as its content.
    Box(modifier, contentAlignment = contentAlignment) {
        AnimatedContent(target, transitionSpec = { dataSwap() }, contentAlignment = contentAlignment, label = "swap") { content(it) }
    }
}

/** [Text] that changes its words by [Swap] and its colour smoothly; [textAlign] also places the words in a wider box. */
@Composable
internal fun SwapText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val ink by animateColorAsState(color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }, Motion.effects(), label = "swapInk")
    val alignment = when (textAlign) {
        TextAlign.End, TextAlign.Right -> Alignment.TopEnd
        TextAlign.Center -> Alignment.TopCenter
        else -> Alignment.TopStart
    }
    Swap(text, modifier, alignment) {
        Text(it, style = style, color = ink, textAlign = textAlign, maxLines = maxLines, softWrap = softWrap, overflow = overflow)
    }
}
