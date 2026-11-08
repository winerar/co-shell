package app.coshell.core.model

/**
 * Parses amounts typed by a user: "1234", "1 234,5", "1,234.56", "-12.30", "0,99".
 *
 * Rules:
 * - spaces (including non-breaking ones) and apostrophes are group separators and are ignored;
 * - the last `.` or `,` is the decimal separator if it is followed by no more digits than the
 *   currency allows; otherwise all `.`/`,` are treated as group separators ("1.234" in a
 *   2-digit currency means 1234);
 * - more fraction digits than the currency allows is an error, never silent rounding.
 */
object MoneyParser {

    sealed interface Result {
        data class Success(val money: Money) : Result
        data class Error(val reason: Reason) : Result
    }

    enum class Reason { EMPTY, INVALID_CHARACTERS, TOO_MANY_FRACTION_DIGITS, TOO_LARGE }

    private val IGNORED = setOf(' ', ' ', ' ', ' ', '\'', '’')

    fun parse(input: String, currency: CurrencyCode): Result {
        val info = Currencies.info(currency)
        var text = input.filterNot { it in IGNORED }
        if (text.isEmpty()) return Result.Error(Reason.EMPTY)

        val negative = text.startsWith('-') || text.startsWith('−')
        if (negative || text.startsWith('+')) text = text.substring(1)
        if (text.isEmpty()) return Result.Error(Reason.EMPTY)
        if (text.any { !it.isAsciiDigit() && it != '.' && it != ',' }) {
            return Result.Error(Reason.INVALID_CHARACTERS)
        }

        val lastSep = text.indexOfLast { it == '.' || it == ',' }
        val intDigits: String
        val fracDigits: String
        if (lastSep < 0) {
            intDigits = text
            fracDigits = ""
        } else {
            val sepChar = text[lastSep]
            val before = text.substring(0, lastSep)
            val after = text.substring(lastSep + 1)
            val mixedSeparators = before.any { (it == '.' || it == ',') && it != sepChar }
            val repeatedSeparator = before.any { it == sepChar }

            val isDecimal = when {
                // "1,234.56": the last, different separator is the decimal one.
                mixedSeparators -> true
                // "1.234.567": the same char repeated can only be grouping.
                repeatedSeparator -> false
                // Single separator: decimal if it fits the currency, "1.234" -> grouping.
                after.length <= info.fractionDigits -> true
                after.length == 3 && before.isNotEmpty() -> false
                else -> true
            }
            if (isDecimal) {
                if (after.length > info.fractionDigits) {
                    return Result.Error(Reason.TOO_MANY_FRACTION_DIGITS)
                }
                intDigits = before.filter { it.isAsciiDigit() }
                fracDigits = after
            } else {
                intDigits = text.filter { it.isAsciiDigit() }
                fracDigits = ""
            }
        }
        if (intDigits.isEmpty() && fracDigits.isEmpty()) return Result.Error(Reason.EMPTY)

        val minorDigits = (intDigits.ifEmpty { "0" } + fracDigits.padEnd(info.fractionDigits, '0'))
            .trimStart('0').ifEmpty { "0" }
        val absolute = minorDigits.toLongOrNull() ?: return Result.Error(Reason.TOO_LARGE)
        return Result.Success(Money(if (negative) -absolute else absolute, currency))
    }

    fun parseOrNull(input: String, currency: CurrencyCode): Money? =
        (parse(input, currency) as? Result.Success)?.money
}

private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'
