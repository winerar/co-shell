package app.coshell.settings

import app.coshell.core.domain.settings.ListDensitySettings
import app.coshell.testing.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ListDensityViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val settings = object : ListDensitySettings {
        override val compact = MutableStateFlow(false)
        override suspend fun setCompact(compact: Boolean) {
            this.compact.value = compact
        }
    }

    @Test
    fun `comfortable by default, compact after the switch, back again`() = runTest {
        val vm = ListDensityViewModel(settings)
        vm.compact.launchIn(backgroundScope)
        assertFalse(vm.compact.value)

        vm.setCompact(true)
        vm.compact.first { it }
        assertTrue(settings.compact.value)

        vm.setCompact(false)
        vm.compact.first { !it }
        assertFalse(settings.compact.value)
    }
}
