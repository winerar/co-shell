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

interface ThemeSettings {
    val mode: Flow<ThemeMode>
    suspend fun setMode(mode: ThemeMode)
}
