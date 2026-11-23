package app.coshell.privacy

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import app.coshell.budgets.list.budgetsPreviewSample
import app.coshell.budgets.list.components.BudgetSummaryCard
import app.coshell.budgets.list.components.EnvelopeRow
import app.coshell.converter.ConverterActions
import app.coshell.converter.ConverterSheetBody
import app.coshell.converter.ConverterUiState
import app.coshell.core.designsystem.amount.HIDDEN_NUMBER
import app.coshell.core.designsystem.amount.LocalAmountsHidden
import app.coshell.core.designsystem.theme.CoShellTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Hidden sums (#600): «Бюджеты» hide every amount, bare numbers in phrases too («потрачено 640 из 1 180 USD»);
 * the converter (#601) keeps its numbers — they are not the person's money.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class HiddenSumsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `budget summary and envelope rows hide bare and coded sums`() {
        val state = budgetsPreviewSample
        val summary = checkNotNull(state.summary)
        show(hidden = true) {
            Column {
                BudgetSummaryCard(summary, closest = emptyList())
                state.envelopes.forEach { EnvelopeRow(it, onClick = {}) }
            }
        }
        // «640», «1 180 USD», «310 000 / 400 000 ARS», «осталось 220 000 ARS · 11 600 ARS в день»…
        listOf("640", "1 180", "310", "400", "220", "11 600", "15 000").forEach { digits ->
            assertEquals("«$digits» is shown while sums are hidden", 0, count(digits))
        }
        assertTrue(count(HIDDEN_NUMBER) > 0)
    }

    @Test
    fun `budget summary shows sums when not hidden`() {
        show(hidden = false) { BudgetSummaryCard(checkNotNull(budgetsPreviewSample.summary), closest = emptyList()) }
        assertTrue(count("640") > 0)
        assertEquals(0, count(HIDDEN_NUMBER))
    }

    @Test
    fun `converter keeps its numbers while sums are hidden`() {
        val state = ConverterUiState(
            isLoading = false, from = "USD", to = "ARS", fromText = "100", toText = "101 500", isEmpty = false,
            rate = "1 015", options = listOf("USD", "ARS"), source = "USD",
        )
        show(hidden = true) { ConverterSheetBody(state, ConverterActions(), onClose = {}) }
        assertEquals(0, count("•"))
    }

    private fun count(text: String): Int =
        compose.onAllNodes(hasText(text, substring = true), useUnmergedTree = true).fetchSemanticsNodes().size

    private fun show(hidden: Boolean, content: @Composable () -> Unit) {
        compose.setContent {
            CoShellTheme { CompositionLocalProvider(LocalAmountsHidden provides hidden) { Surface { content() } } }
        }
        compose.waitForIdle()
    }
}
