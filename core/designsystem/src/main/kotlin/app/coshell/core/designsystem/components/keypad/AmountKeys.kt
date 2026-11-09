package app.coshell.core.designsystem.components.keypad

/** A key of [CoShellKeypad]. */
sealed interface KeypadKey {
    data class Digit(val digit: Char) : KeypadKey
    data object TripleZero : KeypadKey
    data object Decimal : KeypadKey
    data object Plus : KeypadKey
    data object Backspace : KeypadKey
    data object Done : KeypadKey
}

/**
 * Typing an amount on [CoShellKeypad]: plain text like `"32000 + 1500,5"` — terms of digits with a `,`
 * decimal, joined by [PLUS]. Pure, so the rules are unit-tested; the sum is parsed by the screen.
 */
object AmountKeys {
    const val PLUS = " + "
    private const val MAX_DIGITS = 12
    private const val THIN = ' '

    /** [text] after [key]; [fractionDigits] — the currency's (0 for AMD, 2 for USD…). [KeypadKey.Done] changes nothing. */
    fun apply(text: String, key: KeypadKey, fractionDigits: Int = 2): String {
        val current = normalize(text)
        val head = current.substringBeforeLast(PLUS, missingDelimiterValue = "")
        val prefix = if (current.contains(PLUS)) head + PLUS else ""
        val term = if (current.contains(PLUS)) current.substringAfterLast(PLUS) else current
        return when (key) {
            KeypadKey.Done -> current
            KeypadKey.Backspace -> if (current.endsWith(PLUS)) current.dropLast(PLUS.length) else current.dropLast(1)
            KeypadKey.Plus -> if (term.isEmpty() || term.endsWith(',')) current else current + PLUS
            KeypadKey.Decimal -> when {
                fractionDigits == 0 || ',' in term -> current
                term.isEmpty() -> prefix + "0,"
                else -> "$current,"
            }
            KeypadKey.TripleZero -> if (term.isEmpty() || term == "0") current else typed(prefix, term, "000", fractionDigits)
            is KeypadKey.Digit -> typed(prefix, term, key.digit.toString(), fractionDigits)
        }
    }

    private fun typed(prefix: String, term: String, digits: String, fractionDigits: Int): String {
        val fraction = term.substringAfter(',', missingDelimiterValue = "")
        if (',' in term && fraction.length + digits.length > fractionDigits) return prefix + term
        if (term.count { it.isDigit() } + digits.length > MAX_DIGITS) return prefix + term
        val next = if (term == "0") digits.trimStart('0').ifEmpty { "0" } else term + digits
        return prefix + next
    }

    /** Text from elsewhere (an edited transaction: `"1 234.5"`) in the keypad's own form. */
    fun normalize(text: String): String {
        val terms = text.split('+')
            .map { part -> part.filter { it.isDigit() || it == ',' || it == '.' }.replace('.', ',') }
            .filter { it.isNotEmpty() }
        val trailingPlus = terms.isNotEmpty() && text.trimEnd().endsWith('+')
        return terms.joinToString(PLUS) + if (trailingPlus) PLUS else ""
    }

    /** For the screen: thousands split by thin spaces, `"0"` for nothing typed. */
    fun display(text: String): String {
        if (text.isEmpty()) return "0"
        return text.split(PLUS).joinToString(PLUS) { term ->
            val int = term.substringBefore(',')
            val grouped = int.reversed().chunked(3).joinToString(THIN.toString()).reversed()
            if (',' in term) grouped + "," + term.substringAfter(',') else grouped
        }
    }
}
