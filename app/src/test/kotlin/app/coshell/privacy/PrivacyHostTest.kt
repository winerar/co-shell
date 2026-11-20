package app.coshell.privacy

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import app.coshell.core.domain.privacy.PinAttempts
import app.coshell.core.domain.privacy.PinHasher
import app.coshell.core.domain.privacy.PrivacyOptions
import app.coshell.core.domain.privacy.PrivacySettings
import app.coshell.core.domain.privacy.StoredPin
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The lock covers sheets too (#145): a sheet is a window of its own, so an app that was only covered by the lock
 * screen kept an open sheet above it. Locked, the app leaves composition; unlocked, the same sheet comes back.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = android.app.Application::class)
class PrivacyHostTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private class Settings : PrivacySettings {
        override val options = MutableStateFlow(PrivacyOptions(pinEnabled = true))
        override suspend fun update(change: (PrivacyOptions) -> PrivacyOptions) { options.value = change(options.value) }
        override suspend fun pin() = StoredPin("s", "s:1111")
        override suspend fun setPin(pin: StoredPin?) = Unit
        override suspend fun attempts() = PinAttempts()
        override suspend fun setAttempts(attempts: PinAttempts) = Unit
    }

    private val hasher = object : PinHasher {
        override fun newSalt() = "s"
        override fun hash(pin: String, salt: String) = "$salt:$pin"
    }

    /** Like the entry sheet's ViewModel: a half-typed amount must survive the lock. */
    class Typed : ViewModel() {
        var amount = ""
    }

    @Test
    fun `an open sheet hides under the lock and comes back after it`() {
        var now = 0L
        val center = PrivacyCenter(Settings(), hasher, { now }, { now }, EmptyCoroutineContext, CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate))
        rule.setContent {
            PrivacyHost(center, lockScreen = { Text("LOCK") }) {
                val nav = rememberNavController()
                NavHost(nav, startDestination = "home") {
                    composable("home") { Text("HOME", Modifier.clickable { nav.navigate("sheet") }) }
                    dialog("sheet") {
                        var taps by rememberSaveable { mutableIntStateOf(0) }
                        val typed = viewModel<Typed>()
                        Text("SHEET $taps ${typed.amount}", Modifier.clickable { taps++; typed.amount += "5" })
                    }
                }
            }
        }
        rule.onNodeWithText("LOCK").assertIsDisplayed()
        rule.runOnIdle { center.unlock() }
        rule.onNodeWithText("HOME").performClick()
        rule.onNodeWithText("SHEET 0 ").performClick()
        rule.onNodeWithText("SHEET 1 5").assertIsDisplayed()

        rule.runOnIdle {
            center.onBackground()
            now = PrivacyOptions().relock.millis + 1
            center.onForeground()
        }
        rule.waitForIdle()
        rule.onNodeWithText("SHEET 1 5").assertDoesNotExist()
        rule.onNodeWithText("LOCK").assertIsDisplayed()

        rule.runOnIdle { center.unlock() }
        rule.onNodeWithText("SHEET 1 5").assertIsDisplayed()
    }
}
