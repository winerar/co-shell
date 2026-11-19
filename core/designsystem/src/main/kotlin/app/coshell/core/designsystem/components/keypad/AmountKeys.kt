package app.coshell.core.designsystem.components.keypad

/** A key of [CoShellKeypad]. */
sealed interface KeypadKey {
    data class Digit(val digit: Char) : KeypadKey
    data object TripleZero : KeypadKey
    data object Decimal : KeypadKey
    data object Plus : KeypadKey
    data object Minus : KeypadKey
    data object Backspace : KeypadKey
    data object Done : KeypadKey
}

/**
 * Typing an amount on [CoShellKeypad]: plain text like `"32000 + 1500,5 − 200"` — terms of digits with a `,`
 * decimal, joined by [PLUS] / [MINUS] (a change, a discount). Pure, so the rules are unit-tested; the sum is
 * worked out by the screen from [terms].
 */
object AmountKeys {
    const val PLUS = " + "
    const val MINUS = " − "
    private const val MINUS_SIGN = '−'
    private const val MAX_DIGITS = 12
    private const val THIN = '\u2009'

    /** One term of the typed sum: its digits (`"1500,5"`) and whether it is taken away. */
    data class Term(val text: String, val subtract: Boolean)

    /** [text] after [key]; [fractionDigits] — the currency's (0 for AMD, 2 for USD…). [KeypadKey.Done] changes nothing. */
    fun apply(text: String, key: KeypadKey, fractionDigits: Int = 2): String {
        val current = normalize(text)
        val cut = maxOf(current.lastIndexOf(PLUS), current.lastIndexOf(MINUS))
        val prefix = if (cut >= 0) current.substring(0, cut + PLUS.length) else ""
        val term = current.substring(prefix.length)
        return when (key) {
            KeypadKey.Done -> current
            KeypadKey.Backspace -> if (term.isEmpty() && prefix.isNotEmpty()) current.dropLast(PLUS.length) else current.dropLast(1)
            KeypadKey.Plus -> operator(current, prefix, term, PLUS)
            KeypadKey.Minus -> operator(current, prefix, term, MINUS)
            KeypadKey.Decimal -> when {
                fractionDigits == 0 || ',' in term -> current
                term.isEmpty() -> prefix + "0,"
                else -> "$current,"
            }
            KeypadKey.TripleZero -> if (term.isEmpty() || term == "0") current else typed(prefix, term, "000", fractionDigits)
            is KeypadKey.Digit -> typed(prefix, term, key.digit.toString(), fractionDigits)
        }
    }

    /** An operator needs a number before it; pressed right after another operator it replaces it («+» → «−»). */
    private fun operator(current: String, prefix: String, term: String, operator: String): String = when {
        term.endsWith(',') -> current
        term.isNotEmpty() -> current + operator
        prefix.isNotEmpty() -> current.dropLast(PLUS.length) + operator
        else -> current
    }

    private fun typed(prefix: String, term: String, digits: String, fractionDigits: Int): String {
        val fraction = term.substringAfter(',', missingDelimiterValue = "")
        if (',' in term && fraction.length + digits.length > fractionDigits) return prefix + term
        if (term.count { it.isDigit() } + digits.length > MAX_DIGITS) return prefix + term
        val next = if (term == "0") digits.trimStart('0').ifEmpty { "0" } else term + digits
        return prefix + next
    }

    /** Text from elsewhere (an edited transaction: `"1 234.5"`) in the keypad's own form. A leading operator is dropped. */
    fun normalize(text: String): String {
        val out = StringBuilder()
        val term = StringBuilder()
        var pending: String? = null
        var open = false // an operator was typed and no digit follows it yet
        fun flush() {
            if (term.isEmpty()) return
            if (out.isNotEmpty()) out.append(pending ?: PLUS)
            out.append(term)
            term.clear()
        }
        for (ch in text) {
            when {
                ch.isDigit() || ch == ',' -> {
                    term.append(ch)
                    open = false
                }
                ch == '.' -> {
                    term.append(',')
                    open = false
                }
                ch == '+' || ch == MINUS_SIGN -> {
                    flush()
                    pending = if (out.isEmpty()) null else if (ch == '+') PLUS else MINUS
                    open = pending != null
                }
            }
        }
        flush()
        return out.toString() + if (open) pending.orEmpty() else ""
    }

    /** The terms of [text] in order, each with its sign; empty terms are skipped («12 + » → one term). */
    fun terms(text: String): List<Term> {
        val result = mutableListOf<Term>()
        val term = StringBuilder()
        var subtract = false
        fun flush() {
            val digits = term.toString().trim()
            if (digits.isNotEmpty()) result += Term(digits, subtract)
            term.clear()
        }
        for (ch in text) {
            if (ch == '+' || ch == MINUS_SIGN) {
                flush()
                subtract = ch == MINUS_SIGN
            } else {
                term.append(ch)
            }
        }
        flush()
        return result
    }

    /** [text] with [change] applied to every term, operators kept («12,5 + 3,25» → «12 + 3» for `substringBefore(',')`). */
    fun mapTerms(text: String, change: (String) -> String): String {
        val out = StringBuilder()
        val term = StringBuilder()
        fun flush() {
            out.append(change(term.toString()))
            term.clear()
        }
        for (ch in text) {
            if (ch == '+' || ch == MINUS_SIGN) {
                flush()
                out.append(if (ch == '+') PLUS else MINUS)
            } else if (ch != ' ') {
                term.append(ch)
            }
        }
        flush()
        return out.toString()
    }

    /** For the screen: thousands split by thin spaces, `"0"` for nothing typed. */
    fun display(text: String): String {
        if (text.isEmpty()) return "0"
        return buildString {
            val term = StringBuilder()
            fun flush() {
                val int = term.toString().substringBefore(',')
                val grouped = int.reversed().chunked(3).joinToString(THIN.toString()).reversed()
                append(if (',' in term) grouped + "," + term.toString().substringAfter(',') else grouped)
                term.clear()
            }
            for (ch in text) {
                if (ch == '+' || ch == MINUS_SIGN) {
                    flush()
                    append(if (ch == '+') PLUS else MINUS)
                } else if (ch != ' ') {
                    term.append(ch)
                }
            }
            flush()
        }
    }
}
