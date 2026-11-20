package app.coshell.core.domain.privacy

/** The digits typed on the PIN pad (#145): 4 digits, «⌫» removes the last. Pure, the screens keep it in their state. */
data class PinEntry(val digits: String = "") {
    val complete: Boolean get() = digits.length == LENGTH

    fun type(digit: Char): PinEntry =
        if (!digit.isDigit() || complete) this else PinEntry(digits + digit)

    fun erase(): PinEntry = PinEntry(digits.dropLast(1))

    companion object {
        const val LENGTH = 4
    }
}

/** Checks a typed PIN against the saved one. */
fun StoredPin.matches(pin: String, hasher: PinHasher): Boolean = constantTimeEquals(hasher.hash(pin, salt), hash)

private fun constantTimeEquals(a: String, b: String): Boolean {
    if (a.length != b.length) return false
    var diff = 0
    for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
    return diff == 0
}

/** A new [StoredPin] for [pin]. */
fun PinHasher.store(pin: String): StoredPin {
    val salt = newSalt()
    return StoredPin(salt, hash(pin, salt))
}
