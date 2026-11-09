package app.coshell.core.model

/**
 * Locale-independent money formatter. Platform locale data is not used on purpose (KMP-ready);
 * the UI layer picks a [MoneyFormat] preset that matches the user's locale.
 */
data class MoneyFormat(
    val decimalSeparator: Char = ',',
    val groupSeparator: Char = NARROW_NBSP,
    /** Hide the fraction when it is zero: "1 200 RUB" instead of "1 200,00 RUB". */
    val trimZeroFraction: Boolean = false,
) {
    /** «1 234,50 USD»: the number, a no-break space, the letter code (always after the number). */
    fun format(money: Money): String {
        val number = formatNumber(money.amountMinor, Currencies.info(money.currency).fractionDigits)
        return "$number$NBSP${money.currency.value}"
    }

    /** Formats the bare number without a currency code. */
    fun formatNumber(amountMinor: Long, fractionDigits: Int): String {
        val negative = amountMinor < 0
        // Work with the absolute value as a string to stay safe at Long.MIN_VALUE.
        val digits = amountMinor.toString().removePrefix("-").padStart(fractionDigits + 1, '0')
        val intPart = digits.dropLast(fractionDigits)
        val fracPart = digits.takeLast(fractionDigits)

        val grouped = intPart.reversed().chunked(3).joinToString(groupSeparator.toString()).reversed()
        val showFraction = fractionDigits > 0 && !(trimZeroFraction && fracPart.all { it == '0' })
        val body = if (showFraction) "$grouped$decimalSeparator$fracPart" else grouped
        return if (negative) "-$body" else body
    }

    /**
     * The number for an input field: no code, zero fraction trimmed, grouped with narrow spaces
     * so that [MoneyParser] reads it back unambiguously ("1 234,50", "1 200").
     */
    fun formatForEditing(money: Money): String =
        copy(groupSeparator = NARROW_NBSP, trimZeroFraction = true)
            .formatNumber(money.amountMinor, Currencies.info(money.currency).fractionDigits)

    companion object {
        const val NBSP = ' '
        const val NARROW_NBSP = ' '

        /** 1 234,56 RUB */
        val RU = MoneyFormat()

        /** 1,234.56 USD */
        val EN = MoneyFormat(decimalSeparator = '.', groupSeparator = ',')
    }
}
