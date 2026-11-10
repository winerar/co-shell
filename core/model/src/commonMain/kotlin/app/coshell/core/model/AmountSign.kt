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
}
