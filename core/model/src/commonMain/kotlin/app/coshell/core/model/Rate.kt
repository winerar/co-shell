package app.coshell.core.model

/**
 * An exact positive exchange rate: `value = mantissa / 10^scale`.
 *
 * Never a `Double`. Stored as a plain decimal string ([toPlainString] / [parse]).
 * Precision is capped at [MAX_SIGNIFICANT_DIGITS] significant digits and [MAX_SCALE]
 * fraction digits; results of [times] and [inverse] are rounded half to even to that cap.
 * Always normalized (no trailing fraction zeros), so equal rates are `==`.
 */
class Rate private constructor(val mantissa: Long, val scale: Int) : Comparable<Rate> {

    /** Rate of the opposite direction (1 / this), rounded to the precision cap. */
    fun inverse(): Rate {
        // 10^t / mantissa with t = digits + 14 gives at most 15 significant digits.
        val t = digitCount(U128.of(mantissa)) + MAX_SIGNIFICANT_DIGITS - 1
        val quotient = divRoundHalfEven(pow10Wide(t), mantissa)
        return normalize(quotient, t - scale)
    }

    /** Chained rate: (A→B) × (B→C) = A→C. */
    operator fun times(other: Rate): Rate =
        normalize(mulWide(mantissa, other.mantissa), scale + other.scale)

    /** Converts [money] into [to] at this rate, rounding half to even to [to]'s minor unit. */
    fun convert(money: Money, to: CurrencyCode): Money {
        val fromDigits = Currencies.info(money.currency).fractionDigits
        val toDigits = Currencies.info(to).fractionDigits
        val magnitude = if (money.amountMinor == Long.MIN_VALUE) {
            throw ArithmeticException("Long overflow")
        } else {
            kotlin.math.abs(money.amountMinor)
        }
        val product = mulWide(magnitude, mantissa)
        val exponent = toDigits - scale - fromDigits
        val result = if (exponent >= 0) {
            mulWide(product, pow10(exponent))
        } else {
            divRoundHalfEven(product, pow10Wide(-exponent))
        }.toLongExact()
        return Money(if (money.amountMinor < 0) -result else result, to)
    }

    /** Same rate rounded half to even to at most [maxScale] fraction digits (for display). */
    fun rounded(maxScale: Int): Rate {
        require(maxScale in 0..MAX_SCALE)
        if (scale <= maxScale) return this
        return normalize(U128.of(mantissa), scale, maxScale)
    }

    /** "1234.5", "0.00012" — no exponent, no grouping, '.' as separator. */
    fun toPlainString(): String {
        if (scale <= 0) return mantissa.toString() + "0".repeat(-scale)
        val digits = mantissa.toString().padStart(scale + 1, '0')
        val point = digits.length - scale
        return digits.substring(0, point) + "." + digits.substring(point)
    }

    override fun compareTo(other: Rate): Int {
        val common = maxOf(scale, other.scale)
        return timesPow10(U128.of(mantissa), common - scale)
            .compareTo(timesPow10(U128.of(other.mantissa), common - other.scale))
    }

    override fun equals(other: Any?): Boolean =
        other is Rate && other.mantissa == mantissa && other.scale == scale

    override fun hashCode(): Int = 31 * mantissa.hashCode() + scale

    override fun toString(): String = toPlainString()

    companion object {
        const val MAX_SIGNIFICANT_DIGITS = 15
        const val MAX_SCALE = 18

        val ONE: Rate = Rate(1, 0)

        /**
         * Parses plain decimals like "1234.56" or "0,0125" (no exponent). Extra precision is rounded
         * half to even to the cap. Returns `null` for anything that is not a positive number.
         */
        fun parseOrNull(text: String): Rate? {
            val clean = text.trim().replace(',', '.')
            if (!PATTERN.matches(clean)) return null
            val intPart = clean.substringBefore('.')
            val fraction = clean.substringAfter('.', "")
            val digits = (intPart + fraction).trimStart('0').ifEmpty { return null }
            if (digits.length > 38) return null
            var n = U128.of(0)
            for (c in digits) n = addSmall(mulWide(n, 10), c - '0')
            return try {
                normalize(n, fraction.length)
            } catch (e: IllegalArgumentException) {
                null // too small to represent
            }
        }

        /**
         * The real rate of a deal where [from] was paid and [to] received: `1 from.currency = rate to.currency`.
         * E.g. 14.29 USDT debited for a 15 000 ARS receipt → 1 USDT = 1049.69 ARS.
         */
        fun between(from: Money, to: Money): Rate {
            require(from.isPositive && to.isPositive) { "Both amounts must be positive" }
            val fromDigits = Currencies.info(from.currency).fractionDigits
            val toDigits = Currencies.info(to.currency).fractionDigits
            // value = to.minor * 10^fromDigits / (from.minor * 10^toDigits)
            val divisor = mulWide(from.amountMinor, pow10(toDigits))
            val numeratorDigits = digitCount(U128.of(to.amountMinor))
            // Shift the numerator so the quotient keeps ~15 significant digits, within 128 bits.
            val shift = minOf(digitCount(divisor) - numeratorDigits + MAX_SIGNIFICANT_DIGITS, 38 - numeratorDigits)
            val quotient = if (shift >= 0) {
                divRoundHalfEven(timesPow10(U128.of(to.amountMinor), shift), divisor)
            } else {
                divRoundHalfEven(U128.of(to.amountMinor), mulWide(divisor, pow10(-shift)))
            }
            return normalize(quotient, shift - fromDigits)
        }

        fun parse(text: String): Rate =
            requireNotNull(parseOrNull(text)) { "Not a positive rate: '$text'" }

        private val PATTERN = Regex("""\d+(\.\d+)?""")

        private fun addSmall(n: U128, digit: Int): U128 {
            val lo = n.lo + digit.toULong()
            return U128(if (lo < n.lo) n.hi + 1uL else n.hi, lo)
        }

        /** Rounds `n / 10^scale` to the precision cap and strips trailing zeros. */
        private fun normalize(n: U128, scale: Int, maxScale: Int = MAX_SCALE): Rate {
            require(!n.isZero) { "Rate must be positive" }
            val drop = maxOf(digitCount(n) - MAX_SIGNIFICANT_DIGITS, scale - maxScale, 0)
            require(drop <= 38) { "Rate is below the smallest supported value" }
            val rounded = if (drop == 0) n else divRoundHalfEven(n, pow10Wide(drop))
            require(!rounded.isZero) { "Rate is below the smallest supported value" }
            var mantissa = rounded.toLongExact()
            var s = scale - drop
            while (s > 0 && mantissa % 10 == 0L) {
                mantissa /= 10
                s--
            }
            if (s < 0) {
                mantissa = multiplyExact(mantissa, pow10(-s))
                s = 0
            }
            return Rate(mantissa, s)
        }
    }
}
