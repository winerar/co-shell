package app.coshell.wipe

import app.coshell.core.domain.DataCounts
import app.coshell.core.domain.DataWipeRepository
import app.coshell.core.domain.SeedDefaultCategoriesUseCase
import app.coshell.core.domain.WipeAllDataUseCase
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.MainDispatcherRule
import app.coshell.wipe.WipeDataUiState.Step
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WipeDataViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private class FakeWipe : DataWipeRepository {
        var counts = DataCounts(12, 3418, 46, 8)
        var wiped = 0
        override suspend fun counts() = counts
        override suspend fun wipeAll() { wiped++; counts = DataCounts(0, 0, 0, 0) }
    }

    private val wipe = FakeWipe()
    private val categories = FakeCategoryRepository()
    private var backupName: String? = "co-shell-before-wipe-2026-11-10-0912.json"
    private val vm by lazy {
        WipeDataViewModel(wipe, WipeAllDataUseCase(wipe, SeedDefaultCategoriesUseCase(categories) { "id" }), WipeBackup { backupName })
    }

    @Test
    fun `button unlocks only on the word, in any case`() {
        assertEquals(DataCounts(12, 3418, 46, 8), vm.state.value.counts)
        vm.onWordChange("стер")
        assertFalse(vm.state.value.canWipe)
        vm.wipe()
        assertEquals(0, wipe.wiped)
        vm.onWordChange(" стереть ")
        assertTrue(vm.state.value.canWipe)
    }

    @Test
    fun `wipe saves the copy first and shows where it went`() {
        vm.onWordChange("СТЕРЕТЬ")
        vm.wipe()
        assertEquals(1, wipe.wiped)
        assertEquals(Step.DONE, vm.state.value.step)
        assertEquals("co-shell-before-wipe-2026-11-10-0912.json", vm.state.value.backupName)
    }

    @Test
    fun `failed copy wipes nothing and says so`() {
        backupName = null
        vm.onWordChange("СТЕРЕТЬ")
        vm.wipe()
        assertEquals(0, wipe.wiped)
        assertEquals(Step.CONFIRM, vm.state.value.step)
        assertTrue(vm.state.value.backupFailed)
    }
}
