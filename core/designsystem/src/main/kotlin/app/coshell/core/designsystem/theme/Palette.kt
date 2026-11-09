package app.coshell.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colour palettes of the app (owner's choice 2026-11-09, design-questions §9). Picked in
 * «Настройки → Тема»; each has a light and a dark variant. [Volt] — the acid one — is the default.
 * Working names in the design docs: A+ «Кислота» = Volt, A «Лайм-ночь» = Mojito, B «Закат» = Mango,
 * C «Электрик» = Ultra; Lagoon and Sakura were added on the owner's pick 2026-11-09. Specs — `PaletteSpecs.kt`.
 */
enum class CoShellPalette {
    /** Acid lime #B8FF2E, cyan income, violet accent, acid-yellow warmth. Default. */
    Volt,

    /** Soft lime #91E668, violet accent, sky-blue income. */
    Mojito,

    /** Warm sunset: mango #FF8A3D, raspberry accent, gold. */
    Mango,

    /** Cold neon: electric blue #3D7BFF, magenta accent, lime. */
    Ultra,

    /** Sea and sun: turquoise #2EE6C5, coral accent, sand. */
    Lagoon,

    /** Soft and light: pink #FF7AB8, mint accent, lavender. */
    Sakura,
    ;

    internal val spec: PaletteSpec
        get() = when (this) {
            Volt -> VoltSpec
            Mojito -> MojitoSpec
            Mango -> MangoSpec
            Ultra -> UltraSpec
            Lagoon -> LagoonSpec
            Sakura -> SakuraSpec
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
