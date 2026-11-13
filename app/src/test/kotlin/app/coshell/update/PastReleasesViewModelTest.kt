package app.coshell.update

import app.coshell.core.domain.update.InstalledVersion
import app.coshell.core.domain.update.ReleaseHistorySource
import app.coshell.core.domain.update.ReleaseNote
import app.coshell.testing.MainDispatcherRule
import app.coshell.update.past.PastReleasesState
import app.coshell.update.past.PastReleasesViewModel
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PastReleasesViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private class FakeHistory(var result: () -> List<ReleaseNote>) : ReleaseHistorySource {
        override suspend fun history() = result()
    }

    private fun rel(code: Int) = ReleaseNote(code, "0.$code", null, "• $code")

    @Test
    fun `shows installed and older releases, newest first`() {
        val vm = PastReleasesViewModel(FakeHistory { listOf(rel(9), rel(11), rel(10)) }, InstalledVersion(10, "0.10"))
        assertEquals(PastReleasesState.Loaded(listOf(rel(10), rel(9)), 10), vm.state.value)
    }

    @Test
    fun `failure offers retry that loads again`() {
        val source = FakeHistory { throw IOException("offline") }
        val vm = PastReleasesViewModel(source, InstalledVersion(10, "0.10"))
        assertEquals(PastReleasesState.Failed, vm.state.value)
        source.result = { listOf(rel(10)) }
        vm.load()
        assertEquals(PastReleasesState.Loaded(listOf(rel(10)), 10), vm.state.value)
    }
}
