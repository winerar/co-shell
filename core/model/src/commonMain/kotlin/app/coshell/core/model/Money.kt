package app.coshell.core.model

/**
 * An exact amount of money in a single currency.
 *
 * Stored as a whole number of minor units (kopecks, cents, satoshi…) — never as a floating
 * point number. Arithmetic is only allowed between amounts of the same currency; converting
 * between currencies is the job of the exchange-rate layer, not of this class.
 */
data class Money(
    val amountMinor: Long,
    val currency: CurrencyCode,
) : Comparable<Money> {

    val isZero: Boolean get() = amountMinor == 0L
    val isNegative: Boolean get() = amountMinor < 0L
    val isPositive: Boolean get() = amountMinor > 0L

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amountMinor = addExact(amountMinor, other.amountMinor))
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amountMinor = addExact(amountMinor, negateExact(other.amountMinor)))
    }

    operator fun unaryMinus(): Money = copy(amountMinor = negateExact(amountMinor))

    fun abs(): Money = if (isNegative) -this else this

    /**
     * This amount × [numerator] / [denominator], rounded half to even to the minor unit
     * (a share of a limit by days, an average over months). The product is 128-bit, so it never overflows midway.
     */
    fun scale(numerator: Long, denominator: Long): Money {
        require(numerator >= 0) { "numerator must not be negative" }
        require(denominator > 0) { "denominator must be positive" }
        val magnitude = if (isNegative) negateExact(amountMinor) else amountMinor
        val result = divRoundHalfEven(mulWide(magnitude, numerator), denominator).toLongExact()
        return copy(amountMinor = if (isNegative) -result else result)
    }

    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return amountMinor.compareTo(other.amountMinor)
    }

    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) {
            "Currency mismatch: $currency vs ${other.currency}"
        }
    }

    override fun toString(): String = "$amountMinor ${currency.value} (minor units)"

    companion object {
        fun zero(currency: CurrencyCode): Money = Money(0L, currency)

        /**
         * Builds an amount from major and minor parts, e.g. `ofMajor(12, 50, RUB)` = 12.50 RUB.
         * The sign is taken from [major] (or from [minor] when major is 0).
         */
        fun ofMajor(major: Long, minor: Long = 0, currency: CurrencyCode): Money {
            val info = Currencies.info(currency)
            require(minor in -(info.minorPerMajor - 1)..(info.minorPerMajor - 1)) {
                "minor must have at most ${info.fractionDigits} digits for $currency"
            }
            require(major == 0L || minor >= 0L) { "Put the sign on major only" }
            val scaled = multiplyExact(major, info.minorPerMajor)
            val total = if (major < 0) addExact(scaled, -minor) else addExact(scaled, minor)
            return Money(total, currency)
        }
    }
}

/** List sum; all items must share [currency]. */
fun Iterable<Money>.sum(currency: CurrencyCode): Money =
    fold(Money.zero(currency)) { acc, money -> acc + money }

// Overflow-checked arithmetic in pure Kotlin (Math.addExact is JVM-only).

internal fun addExact(a: Long, b: Long): Long {
    val result = a + b
    if (((a xor result) and (b xor result)) < 0) throw ArithmeticException("Long overflow")
    return result
}

internal fun negateExact(a: Long): Long {
    if (a == Long.MIN_VALUE) throw ArithmeticException("Long overflow")
    return -a
}

internal fun multiplyExact(a: Long, b: Long): Long {
    if (a == 0L || b == 0L) return 0L
    val result = a * b
    if (result / b != a || (a == -1L && b == Long.MIN_VALUE) || (b == -1L && a == Long.MIN_VALUE)) {
        throw ArithmeticException("Long overflow")
    }
    return result
}
