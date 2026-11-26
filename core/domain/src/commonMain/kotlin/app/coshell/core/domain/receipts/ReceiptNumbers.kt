package app.coshell.core.domain.receipts

/**
 * Sums as receipts print them (#742): «11.997,00», «65.450» (a dot before three digits is thousands in Argentina), «3,500 AMD»,
 * «1 840,00», «$24.98». A building block for reading receipts no format knows (#742) by rules: which mark is the decimal one.
 */
object ReceiptNumbers {
    /** A sum printed in the text: [whole] and [fraction] digits, the [line] it stands on. */
    data class Printed(val whole: String, val fraction: String, val line: String) {
        val digits: String get() = whole + fraction

        /** Minor units for a currency with [fractionDigits]; `null` when the printed fraction is longer and not zeros. */
        fun minor(fractionDigits: Int): Long? {
            val f = fraction.padEnd(fractionDigits, '0')
            if (f.length > fractionDigits && f.drop(fractionDigits).any { it != '0' }) return null
            return (whole.trimStart('0').ifEmpty { "0" } + f.take(fractionDigits)).toLongOrNull()
        }
    }

    private val NUMBER = Regex("""(?<![\d.,])(\d{1,3}(?:[ \u00A0\u202F.,']\d{3})+(?:[.,]\d{1,8})?|\d+(?:[.,]\d{1,8})?)(?!\d)""")

    /** Every printed number of [text], in order. */
    fun all(text: String): List<Printed> = text.lines().flatMap { line ->
        NUMBER.findAll(line).mapNotNull { read(it.value, line) }.toList()
    }

    /**
     * One number: both «.» and «,» — the last one is the decimal mark; one kind used several times, or once before exactly three
     * digits — thousands; else the decimal mark. Spaces and «'» are always thousands.
     */
    fun read(token: String, line: String = token): Printed? {
        val t = token.filterNot { it == ' ' || it == '\u00A0' || it == '\u202F' || it == '\'' }
        if (t.isEmpty() || !t.first().isDigit()) return null
        val marks = t.filter { it == '.' || it == ',' }
        if (marks.isEmpty()) return Printed(t, "", line)
        val last = t.last { it == '.' || it == ',' }
        val after = t.substringAfterLast(last)
        val thousands = marks.toSet().size == 1 && (marks.length > 1 || after.length == 3)
        return if (thousands) Printed(t.filter(Char::isDigit), "", line)
        else Printed(t.substringBeforeLast(last).filter(Char::isDigit), after, line)
    }
}
