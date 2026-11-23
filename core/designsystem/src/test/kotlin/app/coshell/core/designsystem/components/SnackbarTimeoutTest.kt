package app.coshell.core.designsystem.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import app.coshell.core.designsystem.theme.CoShellTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The app's snackbar host closes a timed offer by itself (#624): «Операция удалена · Отменить» used to stay forever. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SnackbarTimeoutTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun `timed snackbar is dismissed after its duration`() {
        val state = SnackbarHostState()
        val result = mutableStateOf<SnackbarResult?>(null)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CoShellTheme {
                CoShellSnackbarHost(state)
                LaunchedEffect(Unit) {
                    result.value = state.showSnackbar("Deleted", actionLabel = "Undo", duration = SnackbarDuration.Long)
                }
            }
        }
        rule.mainClock.advanceTimeBy(5_000)
        assertNull(result.value)
        rule.mainClock.advanceTimeBy(6_000)
        rule.waitForIdle()
        assertEquals(SnackbarResult.Dismissed, result.value)
        assertNull(state.currentSnackbarData)
    }
}
