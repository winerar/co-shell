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
 * Colour palette of the app («Настройки → Тема», owner's choice 2026-11-09). Device-only like [ThemeMode].
 * [VOLT] — acid lime, the default; [MOJITO] — soft lime; [MANGO] — warm sunset; [ULTRA] — electric blue;
 * [LAGOON] — turquoise and coral; [SAKURA] — pink and mint.
 */
enum class ThemePalette {
    VOLT,
    MOJITO,
    MANGO,
    ULTRA,
    LAGOON,
    SAKURA,
    ;

    companion object {
        /** Palette saved as [name]; unknown or missing → [VOLT]. */
        fun parse(stored: String?): ThemePalette = entries.firstOrNull { it.name == stored } ?: VOLT
    }
}

interface ThemeSettings {
    val mode: Flow<ThemeMode>
    suspend fun setMode(mode: ThemeMode)

    val palette: Flow<ThemePalette>
    suspend fun setPalette(palette: ThemePalette)
}
