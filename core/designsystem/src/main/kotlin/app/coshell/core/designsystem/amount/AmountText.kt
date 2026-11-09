package app.coshell.core.designsystem.amount

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em

/**
 * A formatted amount («1 234,50 USD», from `MoneyFormat`): the number in [color], the currency
 * code after it smaller and grey (owner's choice, design-questions §3). Fraction digits stay the
 * same size. [minFontSize] — shrink a long amount down to it instead of wrapping.
 */
@Composable
fun AmountText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    minFontSize: TextUnit = TextUnit.Unspecified,
) {
    val codeColor = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = remember(text, codeColor) { amountWithCode(text, SpanStyle(color = codeColor, fontSize = CODE_SIZE)) }
    Text(
        text = annotated,
        style = style,
        color = color,
        modifier = modifier,
        maxLines = 1,
        autoSize = if (minFontSize == TextUnit.Unspecified) null
        else TextAutoSize.StepBased(minFontSize = minFontSize, maxFontSize = style.fontSize),
    )
}

/** Code relative to the number: noticeably smaller, still readable in list rows. */
internal val CODE_SIZE = 0.8.em

internal const val NBSP = '\u00A0'

/**
 * Splits a formatted amount into the number and the currency code: the code is the part after the
 * last no-break space, when it has no digits (`MoneyFormat` puts «NBSP + code» after the number;
 * groups use a narrow space). No code → `null`.
 */
fun splitAmount(text: String): Pair<String, String?> {
    val split = text.lastIndexOf(NBSP)
    val code = if (split < 0) "" else text.substring(split + 1)
    if (code.isEmpty() || code.any { it.isDigit() }) return text to null
    return text.substring(0, split) to code
}

/** Applies [codeStyle] to the currency code ([splitAmount]); anything else is returned unstyled. */
fun amountWithCode(text: String, codeStyle: SpanStyle): AnnotatedString {
    val (number, code) = splitAmount(text)
    if (code == null) return AnnotatedString(text)
    return buildAnnotatedString {
        append(number)
        append(NBSP)
        withStyle(codeStyle) { append(code) }
    }
}
