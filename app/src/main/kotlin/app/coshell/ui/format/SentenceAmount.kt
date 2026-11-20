package app.coshell.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.model.MoneyFormat

/**
 * An amount inside a sentence may wrap before its currency code (#379). [MoneyFormat] glues «999 USD» with a no-break space,
 * which is right for a standalone amount; in a phrase at 320 dp × 2.0 the glued word is wider than the line and the text
 * engine would split the code itself («US / D»).
 */
fun String.breakableBeforeCurrency(): String = replace(MoneyFormat.NBSP, ' ')

/** A phrase with sums as shown now: [breakableBeforeCurrency] + hidden sums masked (#145: «потрачено •••• USD из •••• USD»). */
@Composable
@ReadOnlyComposable
fun String.sentenceAmounts(): String = shownAmount(this).breakableBeforeCurrency()
