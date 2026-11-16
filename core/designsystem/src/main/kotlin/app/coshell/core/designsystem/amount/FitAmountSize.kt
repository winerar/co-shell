package app.coshell.core.designsystem.amount

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Auto-size for amounts that is never cut (#432, #433): the largest size from [max] down to [min] that fits, as
 * `TextAutoSize.StepBased`; when even [min] does not fit (huge amount, narrow screen, large system font), the text
 * shrinks below it to exactly fit — a cut amount shows a wrong number («999 999 9»), a small one is still true.
 */
fun fitAmountAutoSize(min: TextUnit, max: TextUnit): TextAutoSize = FitAmountSize(min, max)

private data class FitAmountSize(val min: TextUnit, val max: TextUnit) : TextAutoSize {
    private val stepped = TextAutoSize.StepBased(minFontSize = min, maxFontSize = max)

    override fun TextAutoSizeLayoutScope.getFontSize(constraints: Constraints, text: AnnotatedString): TextUnit {
        val preferred = with(stepped) { getFontSize(constraints, text) }
        if (!constraints.hasBoundedWidth || !performLayout(constraints, text, preferred).hasVisualOverflow) return preferred
        val natural = performLayout(Constraints(), text, preferred).size.width
        var size = shrunkToFit(preferred, natural, constraints.maxWidth)
        // Glyph widths do not scale exactly with the size (hinting): a few small steps down finish the job.
        repeat(FINISH_STEPS) {
            if (!performLayout(constraints, text, size).hasVisualOverflow || size.value <= FLOOR.value) return size
            size = (size.value * FINISH_FACTOR).sp
        }
        return size
    }
}

/** The size at which text [naturalPx] wide at [size] becomes [widthPx] wide; never below [FLOOR]. */
internal fun shrunkToFit(size: TextUnit, naturalPx: Int, widthPx: Int): TextUnit {
    if (naturalPx <= 0 || naturalPx <= widthPx) return size
    return maxOf(size.value * widthPx / naturalPx, FLOOR.value).sp
}

/** Below this nothing is readable at all; the text is cut rather than turned into a line of dots. */
internal val FLOOR = 4.sp
private const val FINISH_STEPS = 4
private const val FINISH_FACTOR = 0.95f
