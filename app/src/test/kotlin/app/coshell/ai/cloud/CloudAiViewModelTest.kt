package app.coshell.ai.cloud

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import app.coshell.core.data.auth.AuthGateway
import app.coshell.core.data.auth.AuthSession
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** «Облачный разбор» (#635): the switch asks first, a missing sign-in dims it, the day's quota is shown only on its own day. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = Application::class)
class CloudAiViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val now = Instant.fromEpochMilliseconds(5L * 86_400_000L + 1_000)
    private val clock = object : Clock { override fun now() = now }

    private class Auth(signedIn: Boolean) : AuthGateway {
        override val session: StateFlow<AuthSession?> = MutableStateFlow(if (signedIn) AuthSession("u", "e@x.com", "N", "t", "r", 0) else null)
        override suspend fun signInWithGoogle(googleIdToken: String): AuthSession = error("not used")
        override fun signOut() = Unit
        override suspend fun deleteAccount() = Unit
        override suspend fun idToken(): String = "t"
    }

    private fun settings() = PrefsCloudAiSettings(ApplicationProvider.getApplicationContext())

    private fun TestScope.viewModel(settings: PrefsCloudAiSettings, signedIn: Boolean = true, url: String = "https://w.example") =
        CloudAiViewModel(settings, Auth(signedIn), clock, url).also { it.state.onEach { }.launchIn(backgroundScope + UnconfinedTestDispatcher(testScheduler)) }

    @Test
    fun `turning on asks first and only the confirmation switches it on`() = runTest {
        val settings = settings()
        val vm = viewModel(settings)
        vm.onToggle(true)
        assertTrue(vm.state.value.askConsent)
        assertFalse(settings.enabled)
        vm.onDismiss()
        assertFalse(vm.state.value.askConsent)
        assertFalse(settings.enabled)
        vm.onToggle(true)
        vm.onConfirm()
        assertTrue(settings.enabled)
        assertEquals(CloudAiStage.ON, vm.state.value.stage)
        vm.onToggle(false)
        assertFalse(settings.enabled)
        assertEquals(CloudAiStage.OFF, vm.state.value.stage)
    }

    @Test
    fun `without a sign in the stage says so and the question never opens`() = runTest {
        val vm = viewModel(settings(), signedIn = false)
        vm.onToggle(true)
        assertEquals(CloudAiStage.NO_LOGIN, vm.state.value.stage)
        assertFalse(vm.state.value.askConsent)
    }

    @Test
    fun `a build without the Worker is not configured`() = runTest {
        assertFalse(viewModel(settings(), url = "").state.value.configured)
        assertTrue(viewModel(settings()).state.value.configured)
    }

    @Test
    fun `the quota shows only on its own day`() = runTest {
        val settings = settings()
        val vm = viewModel(settings)
        assertNull(vm.state.value.left)
        settings.recordQuota(87, 100, day = 4)
        assertNull(vm.state.value.left)
        settings.recordQuota(87, 100, day = 5)
        assertEquals(87, vm.state.value.left)
        assertEquals(100, vm.state.value.limit)
    }
}
