package app.coshell.core.designsystem.category

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BankColorsTest {
    @Test
    fun suggestsByNameWordIgnoringCase() {
        assertEquals("Galicia", BankColors.suggest("galicia ARS")?.name)
        assertEquals("Сбер", BankColors.suggest("Карта Сбербанк")?.name)
        assertEquals("Mercado Pago", BankColors.suggest("Mercado Pago · Аня")?.name)
        assertEquals("Т-Банк", BankColors.suggest("Тинькофф")?.name)
    }

    @Test
    fun noSuggestionForUnknownOrPartialWords() {
        assertNull(BankColors.suggest("Наличные"))
        assertNull(BankColors.suggest("Mpesa"))
    }

    @Test
    fun presetsHaveUniqueNamesAndNoPaletteClash() {
        val names = BankColors.presets.map { it.name }
        assertEquals(names.size, names.toSet().size)
        // A preset equal to a palette swatch would be re-tinted in the light theme.
        assertEquals(emptyList<Long>(), BankColors.presets.map { it.color }.filter { it in CategoryPalette.colors })
    }

    @Test
    fun byColorFindsThePreset() {
        assertEquals("Galicia", BankColors.byColor(0xFFF26E22)?.name)
        assertNull(BankColors.byColor(null))
    }
}
