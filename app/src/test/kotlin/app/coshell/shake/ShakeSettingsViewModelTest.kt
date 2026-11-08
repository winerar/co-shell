package app.coshell.shake

import app.coshell.core.domain.shake.ShakeSettings
import app.coshell.testing.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class ShakeSettingsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val settings = object : ShakeSettings {
        override val enabled = MutableStateFlow(true)
        override suspend fun setEnabled(enabled: Boolean) {
            this.enabled.value = enabled
        }
    }

    @Test
    fun `shows the saved value and saves a flip`() = runTest {
        val vm = ShakeSettingsViewModel(settings)
        vm.enabled.launchIn(backgroundScope)
        vm.enabled.first { it }

        vm.setEnabled(false)
        vm.enabled.first { !it }
        assertFalse(settings.enabled.value)
    }
}
