package app.coshell.core.model

/**
 * Sign toggle for a typed amount ("±" button): many decimal keyboards have no minus key,
 * so negative amounts (credit card balances) are entered by flipping the sign.
 */
object AmountSign {
    private val MINUSES = setOf('-', '−')

    /** "12,5" → "-12,5", "-12,5" / "−12,5" → "12,5", "" → "-"; leading spaces are kept out. */
    fun toggle(text: String): String {
        val trimmed = text.trimStart()
        return when {
            trimmed.firstOrNull() in MINUSES -> trimmed.substring(1)
            trimmed.startsWith('+') -> "-" + trimmed.substring(1)
            else -> "-$trimmed"
        }
    }

    /** Whether the typed amount starts with a minus (`-` or `−`): «-12,5» → `true`. */
    fun isNegative(text: String): Boolean = text.trimStart().firstOrNull() in MINUSES

    /** The digits of [text] without a sign: «-12,5» → «12,5», «+3» → «3». */
    fun magnitude(text: String): String = text.trimStart().let { if (it.firstOrNull() in MINUSES || it.startsWith('+')) it.substring(1) else it }

    /** [digits] with the sign [negative] says; nothing typed has no sign: «12,5», `true` → «-12,5»; «», `true` → «». */
    fun signed(digits: String, negative: Boolean): String = if (negative && digits.isNotBlank()) "-" + magnitude(digits) else magnitude(digits)
}
