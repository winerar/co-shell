package app.coshell.core.model

/**
 * Minimal exact 128-bit arithmetic for non-negative numbers, in pure Kotlin.
 *
 * Needed to multiply an amount by a rate mantissa (both up to ~10^18) without overflow.
 * A value is a pair `hi * 2^64 + lo`, both halves read as unsigned.
 */
internal data class U128(val hi: ULong, val lo: ULong) {
    val isZero: Boolean get() = hi == 0uL && lo == 0uL

    /** Converts back to a [Long]; throws if the value does not fit. */
    fun toLongExact(): Long {
        if (hi != 0uL || lo > Long.MAX_VALUE.toULong()) throw ArithmeticException("Long overflow")
        return lo.toLong()
    }

    operator fun compareTo(other: U128): Int =
        if (hi != other.hi) hi.compareTo(other.hi) else lo.compareTo(other.lo)

    companion object {
        fun of(value: Long): U128 {
            require(value >= 0) { "U128 holds non-negative values only" }
            return U128(0uL, value.toULong())
        }
    }
}

/** Exact product of two non-negative longs. */
internal fun mulWide(a: Long, b: Long): U128 = mulWide(U128.of(a), b)

/** Exact product of a 128-bit value and a non-negative long; throws on 128-bit overflow. */
internal fun mulWide(a: U128, b: Long): U128 {
    require(b >= 0)
    val m = b.toULong()
    val (loHi, loLo) = mul64(a.lo, m)
    val (hiHi, hiLo) = mul64(a.hi, m)
    if (hiHi != 0uL) throw ArithmeticException("128-bit overflow")
    val hi = hiLo + loHi
    if (hi < hiLo) throw ArithmeticException("128-bit overflow")
    return U128(hi, loLo)
}

/** Full 64×64 → 128-bit unsigned product as (hi, lo). */
private fun mul64(a: ULong, b: ULong): Pair<ULong, ULong> {
    val mask = 0xFFFF_FFFFuL
    val a0 = a and mask
    val a1 = a shr 32
    val b0 = b and mask
    val b1 = b shr 32
    val p00 = a0 * b0
    val p01 = a0 * b1
    val p10 = a1 * b0
    val p11 = a1 * b1
    val middle = (p00 shr 32) + (p01 and mask) + (p10 and mask)
    val lo = (p00 and mask) or (middle shl 32)
    val hi = p11 + (p01 shr 32) + (p10 shr 32) + (middle shr 32)
    return hi to lo
}

/**
 * Divides [n] by a positive [divisor], rounding half to even (banker's rounding).
 * Bitwise long division: only 128 steps — fine for display conversions.
 */
internal fun divRoundHalfEven(n: U128, divisor: U128): U128 {
    require(!divisor.isZero) { "Division by zero" }
    var quotient = U128(0uL, 0uL)
    var rem = U128(0uL, 0uL)
    for (bit in 127 downTo 0) {
        val nextBit = if (bit >= 64) (n.hi shr (bit - 64)) and 1uL else (n.lo shr bit) and 1uL
        val overflow = (rem.hi shr 63) != 0uL // rem * 2 does not fit in 128 bits
        rem = U128((rem.hi shl 1) or (rem.lo shr 63), (rem.lo shl 1) or nextBit)
        if (overflow || rem >= divisor) {
            rem = sub(rem, divisor)
            quotient = if (bit >= 64) {
                U128(quotient.hi or (1uL shl (bit - 64)), quotient.lo)
            } else {
                U128(quotient.hi, quotient.lo or (1uL shl bit))
            }
        }
    }
    // Compare 2*rem with divisor without overflow: rem vs divisor - rem.
    val rest = sub(divisor, rem)
    val roundUp = when {
        rem > rest -> true
        rem < rest -> false
        else -> (quotient.lo and 1uL) == 1uL // exactly half → to even
    }
    return if (roundUp) increment(quotient) else quotient
}

internal fun divRoundHalfEven(n: U128, divisor: Long): U128 = divRoundHalfEven(n, U128.of(divisor))

/** a - b for a >= b. */
private fun sub(a: U128, b: U128): U128 {
    val lo = a.lo - b.lo
    val borrow = if (a.lo < b.lo) 1uL else 0uL
    return U128(a.hi - b.hi - borrow, lo)
}

/** 10^n as a 128-bit value, n in 0..38. */
internal fun pow10Wide(n: Int): U128 {
    require(n in 0..38) { "10^$n does not fit in 128 bits" }
    var result = U128.of(1)
    repeat(n) { result = mulWide(result, 10) }
    return result
}

/** Number of decimal digits (0 has 1 digit). */
internal fun digitCount(n: U128): Int {
    var digits = 1
    var bound = U128.of(10)
    while (digits < 39 && n >= bound) {
        digits++
        if (digits == 39) break
        bound = mulWide(bound, 10)
    }
    return digits
}

private fun increment(v: U128): U128 {
    val lo = v.lo + 1uL
    val hi = if (lo == 0uL) v.hi + 1uL else v.hi
    return U128(hi, lo)
}

/** 10^n as a Long, n in 0..18. */
internal fun pow10(n: Int): Long {
    require(n in 0..18) { "10^$n does not fit in Long" }
    var result = 1L
    repeat(n) { result *= 10 }
    return result
}

/** a × 10^n for any n ≥ 0; throws on 128-bit overflow. */
internal fun timesPow10(a: U128, n: Int): U128 {
    require(n >= 0)
    var result = a
    var left = n
    while (left > 0) {
        val step = minOf(left, 18)
        result = mulWide(result, pow10(step))
        left -= step
    }
    return result
}
