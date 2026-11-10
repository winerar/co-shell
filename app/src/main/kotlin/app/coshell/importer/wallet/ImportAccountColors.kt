package app.coshell.importer.wallet

import app.coshell.core.designsystem.category.BankColors
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.domain.importer.wallet.NewAccountColor

/**
 * Colours for accounts the Wallet import creates (#106): a known bank by name gets its own colour
 * (`BankColors.suggest`), others walk the bright palette with a stride so neighbours differ.
 */
object ImportAccountColors : NewAccountColor {
    /** The 15 bright hues at the start of the palette (steel, silver and graphite read as «no colour»). */
    private val bright: List<Long> = CategoryPalette.colors.take(BRIGHT)

    override fun pick(name: String, index: Int): Long =
        BankColors.suggest(name)?.color ?: bright[(index * STRIDE) % bright.size]

    private const val BRIGHT = 15
    private const val STRIDE = 4 // coprime with 15: all hues before a repeat, adjacent ones far apart
}
