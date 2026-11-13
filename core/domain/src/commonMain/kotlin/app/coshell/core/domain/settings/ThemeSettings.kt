package app.coshell.core.domain.settings

import kotlinx.coroutines.flow.Flow

/** Light or dark look of the app (More → «Тема»). Kept on the device only: each phone picks its own. */
enum class ThemeMode {
    /** Follows the phone's dark mode (default). */
    SYSTEM,
    LIGHT,
    DARK,
    ;

    /** Whether the app is dark with this mode while the phone's dark mode is [systemDark]. */
    fun isDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        /** Mode saved as [name]; unknown or missing → [SYSTEM]. */
        fun parse(stored: String?): ThemeMode = entries.firstOrNull { it.name == stored } ?: SYSTEM
    }
}

/** The two rows of «Внешний вид → Тема» (#271): bright neon or pastel. */
enum class PaletteGroup { JUICY, SOFT }

/**
 * Colour palette of the app, named after fruits (#271, owner's choice 2026-11-12). Device-only like [ThemeMode].
 * Seven juicy ones (the former neon palettes renamed, plus [LEMON]) and seven soft pastel ones. [LIME] is the default.
 * Stored by [name]; names saved before #271 map through [parse] so the phone keeps its choice.
 */
enum class ThemePalette(val group: PaletteGroup) {
    LIME(PaletteGroup.JUICY),
    PASSIONFRUIT(PaletteGroup.JUICY),
    MANGO(PaletteGroup.JUICY),
    BILBERRY(PaletteGroup.JUICY),
    FEIJOA(PaletteGroup.JUICY),
    PITAYA(PaletteGroup.JUICY),
    LEMON(PaletteGroup.JUICY),
    PEACH(PaletteGroup.SOFT),
    LYCHEE(PaletteGroup.SOFT),
    MELON(PaletteGroup.SOFT),
    BLUEBERRY(PaletteGroup.SOFT),
    PLUM(PaletteGroup.SOFT),
    PEAR(PaletteGroup.SOFT),
    GOOSEBERRY(PaletteGroup.SOFT),
    ;

    companion object {
        /** Names saved before the fruit names (#271): Вольт → Лайм, Мохито → Маракуйя, Ультра → Черника… */
        private val legacy = mapOf(
            "VOLT" to LIME,
            "MOJITO" to PASSIONFRUIT,
            "ULTRA" to BILBERRY,
            "LAGOON" to FEIJOA,
            "SAKURA" to PITAYA,
        )

        /** Palette saved as [name] (or a pre-#271 name); unknown or missing → [LIME]. */
        fun parse(stored: String?): ThemePalette =
            entries.firstOrNull { it.name == stored } ?: legacy[stored] ?: LIME
    }
}

interface ThemeSettings {
    val mode: Flow<ThemeMode>
    suspend fun setMode(mode: ThemeMode)

    val palette: Flow<ThemePalette>
    suspend fun setPalette(palette: ThemePalette)
}
