package app.coshell.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colour palettes of the app, named after fruits (#271, owner's choice 2026-11-12); picked in «Внешний вид → Тема»,
 * each with a light and a dark variant. Seven juicy (neon; the former Вольт, Мохито, Манго, Ультра, Лагуна, Сакура
 * renamed — Мохито became the violet [Passionfruit] — plus [Lemon]) and seven soft (pastel). [Lime] is the default.
 * Specs — `PaletteSpecs.kt` (juicy), `PaletteSpecsSoft.kt` (soft).
 */
enum class CoShellPalette {
    /** Acid lime #B8FF2E, cyan income, violet accent, acid-yellow warmth. Default (was «Вольт»). */
    Lime,

    /** Violet #A66BFF with the yellow of the pulp, sky-blue income (replaces «Мохито»). */
    Passionfruit,

    /** Warm sunset: mango #FF8A3D, raspberry accent, gold. */
    Mango,

    /** Cold neon: electric blue #3D7BFF, magenta accent, lime (was «Ультра»). */
    Bilberry,

    /** Sea and sun: turquoise #2EE6C5, coral accent, sand (was «Лагуна»). */
    Feijoa,

    /** Pink #FF7AB8, mint accent, lavender (was «Сакура»). */
    Pitaya,

    /** Sunny yellow #FFE14D, indigo accent, lime. */
    Lemon,

    /** Soft: peach #FFB38A, lavender, mint income. */
    Peach,

    /** Soft: lychee pink #F4A7C0, sage, sky income. */
    Lychee,

    /** Soft: melon #EDD27E, lilac, green income. */
    Melon,

    /** Soft: blueberry #A3BFFA, peach accent, mint income. */
    Blueberry,

    /** Soft: plum #C3A6F0, pear green, teal income. */
    Plum,

    /** Soft: pear #C8E39A, lilac, sky income. */
    Pear,

    /** Soft: gooseberry #9EE6CF, pink accent, blue income. */
    Gooseberry,
    ;

    internal val spec: PaletteSpec
        get() = when (this) {
            Lime -> LimeSpec
            Passionfruit -> PassionfruitSpec
            Mango -> MangoSpec
            Bilberry -> BilberrySpec
            Feijoa -> FeijoaSpec
            Pitaya -> PitayaSpec
            Lemon -> LemonSpec
            Peach -> PeachSpec
            Lychee -> LycheeSpec
            Melon -> MelonSpec
            Blueberry -> BlueberrySpec
            Plum -> PlumSpec
            Pear -> PearSpec
            Gooseberry -> GooseberrySpec
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
    /** Light theme hero card (main block of «Обзор»): gradient stops under the dark text ink. */
    val heroLight: List<Color>,
)
