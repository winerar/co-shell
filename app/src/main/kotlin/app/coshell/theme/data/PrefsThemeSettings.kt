package app.coshell.theme.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.core.domain.settings.ThemeSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [ThemeSettings] in the device "settings" file (not synced, not in the backup). */
@Singleton
class PrefsThemeSettings @Inject constructor(@ApplicationContext context: Context) : ThemeSettings {

    private val prefs by lazy { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<ThemeMode?>(null)

    override val mode: Flow<ThemeMode> = state
        .onStart { if (state.value == null) state.compareAndSet(null, io { ThemeMode.parse(prefs.getString(KEY, null)) }) }
        .filterNotNull()

    override suspend fun setMode(mode: ThemeMode) {
        state.value = mode
        io { prefs.edit { putString(KEY, mode.name) } }
    }

    private val paletteState = MutableStateFlow<ThemePalette?>(null)

    override val palette: Flow<ThemePalette> = paletteState
        .onStart {
            if (paletteState.value == null) {
                paletteState.compareAndSet(null, io { ThemePalette.parse(prefs.getString(PALETTE_KEY, null)) })
            }
        }
        .filterNotNull()

    override suspend fun setPalette(palette: ThemePalette) {
        paletteState.value = palette
        io { prefs.edit { putString(PALETTE_KEY, palette.name) } }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val KEY = "theme_mode"
        const val PALETTE_KEY = "theme_palette"
    }
}
