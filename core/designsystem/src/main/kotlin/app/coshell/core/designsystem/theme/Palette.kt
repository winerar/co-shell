package app.coshell.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colour themes of the app (1.0, owner's choice 2026-11-16, #354): three fruits, three drinks, three plants — each with a
 * main colour (neon or pastel) and a second, near-complementary one; picked in «Внешний вид → Тема», light and dark.
 * [Lime] is the default. Specs — `PaletteSpecs.kt`.
 */
enum class CoShellPalette {
    /** Acid lime #B8FF2E + violet. Default. */
    Lime,

    /** Ruby #FF3D6E + turquoise; expense in coral so it does not merge with the ruby. */
    Garnet,

    /** Banana yellow #FFE135 + indigo. */
    Banana,

    /** Pastel herbal green #B5D98A + copper. */
    Mate,

    /** Coffee brown #C67F4D + electric blue. */
    Espresso,

    /** Amber #FFB020 + turquoise. */
    Kvas,

    /** Pastel blossom pink #FFB3CF + leaf green. */
    Sakura,

    /** Cornflower blue #5C8DFF + wheat gold. */
    Cornflower,

    /** Pastel lilac #CDB4FF + lotus-leaf green. */
    Lotus,
    ;

    internal val spec: PaletteSpec
        get() = when (this) {
            Lime -> LimeSpec
            Garnet -> GarnetSpec
            Banana -> BananaSpec
            Mate -> MateSpec
            Espresso -> EspressoSpec
            Kvas -> KvasSpec
            Sakura -> SakuraSpec
            Cornflower -> CornflowerSpec
            Lotus -> LotusSpec
        }
}

/**
 * The few colours that make a palette; everything else ([lightSchemeOf], [darkSchemeOf]) is derived.
 * [primary] is always a fill with [onPrimary] ink on it (buttons, «+», the selected tab), in both themes.
 * Pairs are dark / light: neon reads on graphite, deeper shades read on white.
 */
@Immutable
internal data class PaletteSpec(
    val primary: Color,
    val onPrimary: Color,
    val accentDark: Color,
    val accentLight: Color,
    val tertiaryDark: Color,
    val tertiaryLight: Color,
    val incomeDark: Color,
    val incomeLight: Color,
    val expenseDark: Color,
    val expenseLight: Color,
    val backgroundDark: Color,
    val backgroundLight: Color,
)
