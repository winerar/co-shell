package app.coshell.ui.form

import app.coshell.core.designsystem.components.keypad.KeypadKey

/**
 * Typing a plain number (a percent, days, the last digits of a card) on our keypad: no sum, no grouping, leading zeros kept
 * («0123» is a card). [maxWhole] digits before the `,`, [fractionDigits] after it (0 — no `,`). Pure, so it is unit-tested;
 * amounts use `AmountKeys` instead. «+», «−» and «Готово» change nothing.
 */
internal object NumberKeys {
    fun apply(text: String, key: KeypadKey, maxWhole: Int, fractionDigits: Int = 0): String {
        val whole = text.substringBefore(',')
        val hasComma = ',' in text
        val fraction = text.substringAfter(',', missingDelimiterValue = "")
        return when (key) {
            is KeypadKey.Digit -> when {
                hasComma -> if (fraction.length < fractionDigits) text + key.digit else text
                else -> if (whole.length < maxWhole) text + key.digit else text
            }
            KeypadKey.TripleZero -> (1..3).fold(text) { acc, _ -> apply(acc, KeypadKey.Digit('0'), maxWhole, fractionDigits) }
            KeypadKey.Decimal -> when {
                fractionDigits == 0 || hasComma -> text
                whole.isEmpty() -> "0,"
                else -> "$text,"
            }
            KeypadKey.Backspace -> text.dropLast(1)
            KeypadKey.Plus, KeypadKey.Minus, KeypadKey.Done -> text
        }
    }

    /** [initial] as the keypad types it: digits and one `,` (a saved «5.5» → «5,5»), cut to the limits. */
    fun normalize(initial: String, maxWhole: Int, fractionDigits: Int = 0): String =
        initial.trim().replace('.', ',').fold("") { acc, c ->
            when (c) {
                in '0'..'9' -> apply(acc, KeypadKey.Digit(c), maxWhole, fractionDigits)
                ',' -> apply(acc, KeypadKey.Decimal, maxWhole, fractionDigits)
                else -> acc
            }
        }
}
