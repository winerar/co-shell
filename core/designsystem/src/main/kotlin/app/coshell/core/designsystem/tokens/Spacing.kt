package app.coshell.core.designsystem.tokens

import androidx.compose.ui.unit.dp

/**
 * Spacing scale on a 4 dp grid. Screens use these names instead of raw `dp` numbers,
 * so the rhythm of the whole app can be tuned in one place.
 */
object Spacing {
    /** Hairline gaps: between an amount and its secondary line. */
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    /** Default screen edge padding and gap between blocks. */
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** All steps from smallest to largest; used by tests and the catalog. */
    val scale = listOf(xxs, xs, s, m, l, xl, xxl)
}
