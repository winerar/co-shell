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

/**
 * Colour theme of the app (1.0, owner's choice 2026-11-16, #354): fruits, drinks, plants — one list. Device-only like
 * [ThemeMode]. [LIME] is the default. Stored by [name]; the 14 fruit palettes of 0.x and the names before them map
 * through [parse] to the nearest new theme, so the phone keeps a close look.
 */
enum class ThemePalette {
    LIME,
    GARNET,
    BANANA,
    MATE,
    ESPRESSO,
    KVAS,
    SAKURA,
    CORNFLOWER,
    LOTUS,
    ;

    companion object {
        /** Palettes of 0.x (#271 fruits and the names before them) → the nearest 1.0 theme. */
        private val legacy = mapOf(
            "VOLT" to LIME,
            "MOJITO" to LOTUS,
            "ULTRA" to CORNFLOWER,
            "LAGOON" to LIME,
            "SAKURA" to SAKURA,
            "PASSIONFRUIT" to LOTUS,
            "MANGO" to KVAS,
            "BILBERRY" to CORNFLOWER,
            "FEIJOA" to LIME,
            "PITAYA" to SAKURA,
            "LEMON" to BANANA,
            "PEACH" to ESPRESSO,
            "LYCHEE" to SAKURA,
            "MELON" to ESPRESSO,
            "BLUEBERRY" to CORNFLOWER,
            "PLUM" to LOTUS,
            "PEAR" to MATE,
            "GOOSEBERRY" to MATE,
        )

        /** Theme saved as [name] (or a 0.x name); unknown or missing → [LIME]. */
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
