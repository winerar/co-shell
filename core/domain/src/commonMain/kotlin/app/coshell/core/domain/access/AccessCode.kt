package app.coshell.core.domain.access

/**
 * Invitation code of the closed test (#666): 12 characters from a 31-letter alphabet without look-alikes (no 0/O, 1/I/L —
 * people read codes aloud and retype them), shown as `K7QX-M2PD-9WTA`. The robot that creates codes
 * (scripts/invite_codes.py) uses the same alphabet and rules.
 */
object AccessCode {
    const val LENGTH = 12
    const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

    /** What a person typed → the 12-character id (upper case, no spaces or dashes); `null` if it cannot be a code. */
    fun normalize(text: String): String? {
        val code = text.uppercase().filter { it.isLetterOrDigit() }
        return code.takeIf { it.length == LENGTH && it.all { c -> c in ALPHABET } }
    }

    /** `K7QXM2PD9WTA` → `K7QX-M2PD-9WTA`. */
    fun display(code: String): String = code.chunked(4).joinToString("-")

    /** The field shows the code as it is typed: upper case, grouped by four, no more than [LENGTH] characters. */
    fun format(typed: String): String =
        display(typed.uppercase().filter { it.isLetterOrDigit() }.take(LENGTH)).let { if (it.endsWith("-")) it.dropLast(1) else it }
}
