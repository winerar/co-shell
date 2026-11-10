package app.coshell.core.domain.importer.wallet

/**
 * Colour (0xAARRGGBB) for an account the import creates: Wallet's CSV has no colours, and grey badges
 * in the accounts list read as a bug (#106). [index] — the account's order among the new ones, so
 * neighbours can get different colours. The palette lives in the UI layer, so the app supplies this.
 */
fun interface NewAccountColor {
    fun pick(name: String, index: Int): Long?

    companion object {
        val None = NewAccountColor { _, _ -> null }
    }
}
