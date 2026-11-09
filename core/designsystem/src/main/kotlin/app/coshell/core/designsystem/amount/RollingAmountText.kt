package app.coshell.core.designsystem.amount

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.coshell.core.designsystem.tokens.Motion

/**
 * The main total with an odometer (owner's choice, design-questions §6): when the amount changes,
 * each changed digit rolls up (amount grew) or down (fell). Same look as [AmountText] — grey smaller
 * code — and it shrinks to fit the width (monospace font: every character is [MONO_ADVANCE] em wide).
 * Only translation and alpha are animated (performance.md).
 */
@Composable
fun RollingAmountText(text: String, style: TextStyle, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    val (number, code) = splitAmount(text)
    val previous = remember { arrayOf(text) }
    val direction = remember(text) { amountDirection(previous[0], text) }
    SideEffect { previous[0] = text }

    BoxWithConstraints(modifier.clipToBounds()) {
        val density = LocalDensity.current
        val ems = number.length * MONO_ADVANCE + (code?.let { MONO_ADVANCE + it.length * MONO_ADVANCE * CODE_SIZE.value } ?: 0f)
        val fitted = fittedSize(style.fontSize, with(density) { maxWidth.toPx() }, ems, density.density * density.fontScale)
        val sized = style.copy(fontSize = fitted, lineHeight = style.lineHeight.takeIf { it != TextUnit.Unspecified }?.let { it * (fitted.value / style.fontSize.value) } ?: TextUnit.Unspecified)
        Row(verticalAlignment = Alignment.Bottom) {
            number.forEachIndexed { index, char ->
                // Keyed from the right, so units stay units when the number gets longer.
                key(number.length - index) {
                    AnimatedContent(targetState = char, transitionSpec = { roll(direction) }, label = "digit") {
                        Text(it.toString(), style = sized, color = color)
                    }
                }
            }
            if (code != null) {
                // Same line box as the digits, smaller span inside: baselines line up in the bottom-aligned row.
                Text(
                    buildAnnotatedString { withStyle(SpanStyle(fontSize = CODE_SIZE)) { append(NBSP + code) } },
                    style = sized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun roll(direction: Int): ContentTransform {
    val up = direction >= 0
    return (slideInVertically(Motion.medium()) { if (up) it else -it } + fadeIn(Motion.medium()))
        .togetherWith(slideOutVertically(Motion.medium()) { if (up) -it else it } + fadeOut(Motion.short()))
}

/** Advance width of JetBrains Mono, in em. */
internal const val MONO_ADVANCE = 0.6f

/** Largest size ≤ [max] at which [ems] of text fit [widthPx]; [pxPerSp] = density × font scale. */
internal fun fittedSize(max: TextUnit, widthPx: Float, ems: Float, pxPerSp: Float): TextUnit {
    if (ems <= 0f || widthPx <= 0f) return max
    val fit = widthPx / ems / pxPerSp
    return if (fit >= max.value) max else fit.sp
}

/**
 * +1 if [new] is a larger amount than [old], −1 if smaller, 0 if equal or not comparable. Reads the
 * digits and a leading minus («−» or «-») of formatted amounts; both use the same fraction digits.
 */
fun amountDirection(old: String, new: String): Int {
    val a = parse(old) ?: return 0
    val b = parse(new) ?: return 0
    if (a.first != b.first) return if (b.first) -1 else 1
    val cmp = compareDigits(a.second, b.second)
    return if (b.first) -cmp else cmp
}

private fun parse(text: String): Pair<Boolean, String>? {
    val (number, _) = splitAmount(text)
    val digits = number.filter { it.isDigit() }.trimStart('0')
    if (number.none { it.isDigit() }) return null
    val negative = number.trimStart().let { it.startsWith('−') || it.startsWith('-') }
    return (negative && digits.isNotEmpty()) to digits
}

private fun compareDigits(a: String, b: String): Int = when {
    a.length != b.length -> if (b.length > a.length) 1 else -1
    else -> b.compareTo(a).coerceIn(-1, 1)
}
