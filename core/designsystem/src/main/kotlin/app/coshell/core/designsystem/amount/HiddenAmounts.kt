package app.coshell.core.designsystem.amount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Sums hidden on screen (#145, board `Priv145HideA`, poll «А»: «•••• USD» — the code stays, the number goes).
 * The app provides it from «глаз» / «экраном вниз»; [AmountText], [RollingAmountText] and the chart labels read it,
 * so every screen hides at once. Previews and tests: `false`.
 */
val LocalAmountsHidden = staticCompositionLocalOf { false }

/** What stands for a hidden number. */
const val HIDDEN_NUMBER = "••••"

private val MONEY = Regex("[−+\\-]?\\d(?:[\\d\\u202F\\u2009 .,]*\\d)?(?=[\\u00A0 ][A-Z]{2,6}(?![A-Za-z]))")

/**
 * Hides every sum in [text]: a number followed by «NBSP + code» (how `MoneyFormat` writes it; a plain space is taken too) becomes [HIDDEN_NUMBER],
 * the sign goes too (it tells how much came or went). A text that is only a number — hidden whole.
 */
fun maskAmounts(text: String): String {
    val (number, code) = splitAmount(text)
    if (code == null && number.any { it.isDigit() } && number.all { it.isDigit() || it in NUMBER_MARKS }) return HIDDEN_NUMBER
    return MONEY.replace(text, HIDDEN_NUMBER)
}

private const val NUMBER_MARKS = "−+-\u202F\u2009\u00A0 .,≈"

/** [text] as it should be shown now: masked while [LocalAmountsHidden]. */
@Composable
@ReadOnlyComposable
fun shownAmount(text: String): String = if (LocalAmountsHidden.current) maskAmounts(text) else text
