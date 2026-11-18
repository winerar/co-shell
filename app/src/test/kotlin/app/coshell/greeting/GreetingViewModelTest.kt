package app.coshell.greeting

import app.coshell.core.data.family.FamilyState
import app.coshell.core.domain.update.InstalledVersion
import app.coshell.core.model.Family
import app.coshell.core.model.Member
import app.coshell.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GreetingViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val owner = Member("u1", "Owner", "owner@example.com")
    private val partner = Member("u2", "Partner", "partner@example.com")
    private val family = Family("f", owner.uid, listOf(owner, partner))
    private val state = MutableStateFlow<FamilyState>(FamilyState.SignedOut)
    private val store = FakeGreetingStore()
    private val notices = FakeNotices()

    private fun vm(version: String) =
        GreetingViewModel({ state }, InstalledVersion(1, version), store, notices)

    @Test
    fun `partner on 1_0 gets the pill and one notification`() = runTest {
        state.value = FamilyState.Joined(family, partner)
        val vm = vm("1.0")
        advanceUntilIdle()
        assertTrue(vm.pillVisible.value)
        assertEquals(1, notices.shown)
        assertTrue(store.notified)
    }

    @Test
    fun `the owner gets nothing`() = runTest {
        state.value = FamilyState.Joined(family, owner)
        val vm = vm("1.0")
        advanceUntilIdle()
        assertFalse(vm.pillVisible.value)
        assertEquals(0, notices.shown)
    }

    @Test
    fun `nothing before 1_0 and nothing without a family`() = runTest {
        state.value = FamilyState.Joined(family, partner)
        val old = vm("0.99")
        advanceUntilIdle()
        assertFalse(old.pillVisible.value)
        state.value = FamilyState.SignedOut
        val out = vm("1.0")
        advanceUntilIdle()
        assertFalse(out.pillVisible.value)
        assertEquals(0, notices.shown)
    }

    @Test
    fun `cross closes the pill for good and the notification is not repeated`() = runTest {
        state.value = FamilyState.Joined(family, partner)
        val vm = vm("1.0")
        advanceUntilIdle()
        vm.dismiss()
        assertFalse(vm.pillVisible.value)
        assertTrue(store.dismissed)
        val again = vm("1.1")
        advanceUntilIdle()
        assertFalse(again.pillVisible.value)
        assertEquals(1, notices.shown)
    }

    @Test
    fun `no permission still counts as the one attempt`() = runTest {
        notices.result = false
        state.value = FamilyState.Joined(family, partner)
        val vm = vm("1.0")
        advanceUntilIdle()
        assertTrue(vm.pillVisible.value)
        assertTrue(store.notified)
    }

    private class FakeGreetingStore : GreetingStore {
        override var notified = false
        override var dismissed = false
    }

    private class FakeNotices : GreetingNotices {
        var shown = 0
        var result = true
        override fun show(): Boolean {
            shown++
            return result
        }
    }
}
