package app.coshell.transactions.list.components

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import app.coshell.core.designsystem.components.PeekLine
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.transactions.list.AmountPeek
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** #318: holding the amount shows the bubble until the finger lifts; a tap on it still opens the row. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp", application = Application::class)
class AmountPeekGestureTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val row = Row(id = "t1", kind = Kind.EXPENSE, title = "Продукты", subtitle = "Galicia", note = "", amount = "−32 000,00 ARS")
    private val bubble = AmountPeek("t1", listOf(PeekLine("31,53", "USD")), LocalDate(2026, 11, 12))

    @Test
    fun `hold shows the bubble, release hides it, tap opens`() {
        var peek by mutableStateOf<AmountPeek?>(null)
        var opened = 0
        var selected = 0
        compose.setContent {
            CoShellTheme {
                TransactionRow(
                    row = row,
                    onClick = { opened++ },
                    onLongClick = { selected++ },
                    peek = peek,
                    onPeek = { peek = bubble },
                    onPeekEnd = { peek = null },
                )
            }
        }
        val amount = compose.onNodeWithText("−32 000,00 ARS", substring = true, useUnmergedTree = true)

        amount.performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(1_000)
        compose.onNodeWithText("≈ 31,53", substring = true, useUnmergedTree = true).assertExists()
        amount.performTouchInput { up() }
        compose.onNodeWithText("≈ 31,53", substring = true, useUnmergedTree = true).assertDoesNotExist()
        assertEquals("the row's long press (selection) does not fire from the amount", 0, selected)

        amount.performClick()
        assertEquals(1, opened)
    }
}
