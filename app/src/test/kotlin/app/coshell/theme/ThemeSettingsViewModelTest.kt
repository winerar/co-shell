package app.coshell.theme

import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.core.domain.settings.ThemeSettings
import app.coshell.testing.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ThemeSettingsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val settings = object : ThemeSettings {
        override val mode = MutableStateFlow(ThemeMode.SYSTEM)
        override suspend fun setMode(mode: ThemeMode) {
            this.mode.value = mode
        }

        override val palette = MutableStateFlow(ThemePalette.VOLT)
        override suspend fun setPalette(palette: ThemePalette) {
            this.palette.value = palette
        }
    }

    @Test
    fun `shows the saved mode and saves a change`() = runTest {
        val vm = ThemeSettingsViewModel(settings)
        vm.mode.first { it == ThemeMode.SYSTEM }

        vm.setMode(ThemeMode.DARK)
        vm.mode.first { it == ThemeMode.DARK }
        assertEquals(ThemeMode.DARK, settings.mode.value)
    }

    @Test
    fun `shows the saved palette and saves a change`() = runTest {
        val vm = ThemeSettingsViewModel(settings)
        vm.palette.first { it == ThemePalette.VOLT }

        vm.setPalette(ThemePalette.MANGO)
        vm.palette.first { it == ThemePalette.MANGO }
        assertEquals(ThemePalette.MANGO, settings.palette.value)
    }
}
