package app.coshell.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colour palettes of the app (owner's choice 2026-11-09, design-questions §9). Picked in
 * «Настройки → Тема»; each has a light and a dark variant. [Volt] — the acid one — is the default.
 * Working names in the design docs: A+ «Кислота» = Volt, A «Лайм-ночь» = Mojito, B «Закат» = Mango,
 * C «Электрик» = Ultra.
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
    ;

    internal val spec: PaletteSpec
        get() = when (this) {
            Volt -> VoltSpec
            Mojito -> MojitoSpec
            Mango -> MangoSpec
            Ultra -> UltraSpec
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

internal val VoltSpec = PaletteSpec(
    primary = Color(0xFFB8FF2E),
    onPrimary = Color(0xFF0E1F00),
    accentDark = Color(0xFFB266FF),
    accentLight = Color(0xFF7A2BD6),
    tertiaryDark = Color(0xFFFFE600),
    tertiaryLight = Color(0xFF6E6200),
    incomeDark = Color(0xFF00F0FF),
    incomeLight = Color(0xFF00708A),
    expenseDark = Color(0xFFFF2E63),
    expenseLight = Color(0xFFD1004B),
    backgroundDark = Color(0xFF050805),
    backgroundLight = Color(0xFFF9FFE9),
    heroLight = listOf(Color(0xFFB8FF2E), Color(0xFF8CFFA0), Color(0xFF5CF6FF)),
)

internal val MojitoSpec = PaletteSpec(
    primary = Color(0xFF91E668),
    onPrimary = Color(0xFF122A06),
    accentDark = Color(0xFFA77BFF),
    accentLight = Color(0xFF6236E0),
    tertiaryDark = Color(0xFF38D9FF),
    tertiaryLight = Color(0xFF00709E),
    incomeDark = Color(0xFF38D9FF),
    incomeLight = Color(0xFF00709E),
    expenseDark = Color(0xFFFF5C7A),
    expenseLight = Color(0xFFD6004A),
    backgroundDark = Color(0xFF0B0F0C),
    backgroundLight = Color(0xFFF7FBF2),
    heroLight = listOf(Color(0xFF91E668), Color(0xFF8FE9C2), Color(0xFF8FE3FF)),
)

internal val MangoSpec = PaletteSpec(
    primary = Color(0xFFFF8A3D),
    onPrimary = Color(0xFF2B0E00),
    accentDark = Color(0xFFFF3D7F),
    accentLight = Color(0xFFC2185B),
    tertiaryDark = Color(0xFFFFC93C),
    tertiaryLight = Color(0xFF7A5600),
    incomeDark = Color(0xFFB6FF3D),
    incomeLight = Color(0xFF3F7300),
    expenseDark = Color(0xFFFF4D5E),
    expenseLight = Color(0xFFC8102E),
    backgroundDark = Color(0xFF160C16),
    backgroundLight = Color(0xFFFFF5EA),
    heroLight = listOf(Color(0xFFFFB27A), Color(0xFFFFC2A0), Color(0xFFFFB3CC)),
)

internal val UltraSpec = PaletteSpec(
    primary = Color(0xFF3D7BFF),
    onPrimary = Color(0xFF000B24),
    accentDark = Color(0xFFFF2BD6),
    accentLight = Color(0xFFB0008F),
    tertiaryDark = Color(0xFF91E668),
    tertiaryLight = Color(0xFF2F6B12),
    incomeDark = Color(0xFF7CFF6B),
    incomeLight = Color(0xFF2E7A00),
    expenseDark = Color(0xFFFF3B6B),
    expenseLight = Color(0xFFC4004A),
    backgroundDark = Color(0xFF07090C),
    backgroundLight = Color(0xFFF4F7FF),
    heroLight = listOf(Color(0xFFA9C4FF), Color(0xFFD4C2FF), Color(0xFFFFB3F0)),
)
