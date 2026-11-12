package app.coshell.navigation

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.coshell.R
import app.coshell.accounts.list.AccountsActions
import app.coshell.accounts.list.AccountsScreen
import app.coshell.accounts.list.AccountsUiState
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.model.AccountType
import app.coshell.home.HomeScreen
import app.coshell.home.HomeUiState
import app.coshell.more.MoreActions
import app.coshell.more.MoreScreen
import app.coshell.transactions.list.Selection
import app.coshell.transactions.list.TransactionsActions
import app.coshell.transactions.list.TransactionsScreen
import app.coshell.transactions.list.TransactionsUiState
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import app.coshell.transactions.list.TransactionsUiState.Row
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import app.coshell.update.notice.UpdatePill
import app.coshell.update.release
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Smoke test of the app's frame (`MainShell`) with the real tab screens on sample data: what a phone
 * shows right after an update. Catches a frame that hides the screens (releases #52/#56: the bottom
 * bar took the whole screen) or tabs that don't switch. No Hilt, Room or network — runs in seconds.
 */
@RunWith(RobolectricTestRunner::class)
// A plain Application: CoShellApp would open Room (native SQLite) and schedule work at start.
@Config(sdk = [36], qualifiers = "w411dp-h891dp", application = Application::class)
class MainShellSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private var adds = 0
    private var pillTaps = 0

    @Before
    fun setUp() {
        compose.setContent {
            CoShellTheme(darkTheme = true) {
                val nav = rememberNavController()
                MainShell(
                    nav,
                    SnackbarHostState(),
                    onAdd = { adds++ },
                    moreBadge = true,
                    overBar = { onBar ->
                        UpdatePill(release(43), visible = onBar, onOpen = { pillTaps++ }, onDismiss = {})
                    },
                ) {
                    composable<HomeDestination> { HomeScreen(HOME) }
                    composable<TransactionsDestination> {
                        var selection by remember { mutableStateOf(Selection()) }
                        TransactionsScreen(
                            TRANSACTIONS,
                            selection,
                            TransactionsActions(
                                onLongPress = { selection = selection.longPress(it) },
                                onToggle = { selection = selection.toggle(it) },
                                onClearSelection = { selection = Selection() },
                            ),
                        )
                    }
                    composable<AccountsDestination> { AccountsScreen(ACCOUNTS, AccountsActions()) }
                    composable<MoreDestination> {
                        MoreScreen(MoreActions(onSettings = { nav.navigate(SettingsDestination) }))
                    }
                    composable<SettingsDestination> { Text(INNER, Modifier.clickable { nav.popBackStack() }) }
                }
            }
        }
    }

    @Test
    fun `screen is visible above a bottom bar that hugs the bottom edge`() {
        compose.onNodeWithText(HOME_ACCOUNT).assertIsDisplayed()
        val screen = compose.onRoot().getBoundsInRoot()
        val add = compose.onNodeWithContentDescription(string(R.string.transaction_add)).getBoundsInRoot()
        val height = screen.bottom - screen.top
        assertTrue("«+» must sit in the bottom quarter, top=${add.top} of $height", add.top > screen.top + height * 3 / 4)
        assertTrue("«+» must be on screen, bottom=${add.bottom} of $height", add.bottom <= screen.bottom)
    }

    @Test
    fun `every tab opens its screen`() {
        tab(R.string.nav_transactions)
        compose.onNodeWithText(TRANSACTION_CATEGORY).assertIsDisplayed()
        tab(R.string.nav_accounts)
        compose.onNodeWithText(ACCOUNT).assertIsDisplayed()
        tab(R.string.nav_more)
        compose.onNodeWithText(string(R.string.categories_title)).assertIsDisplayed()
        tab(R.string.nav_home)
        compose.onNodeWithText(HOME_ACCOUNT).assertIsDisplayed()
    }

    @Test
    fun `plus opens a new transaction`() {
        compose.onNodeWithContentDescription(string(R.string.transaction_add)).performClick()
        compose.waitForIdle()
        assertEquals(1, adds)
    }

    @Test
    fun `bar rides away on an inner screen and comes back on return`() {
        openSettings()
        compose.waitForIdle()
        compose.onNodeWithText(INNER).assertIsDisplayed()
        compose.onNodeWithContentDescription(string(R.string.transaction_add)).assertDoesNotExist()

        compose.onNodeWithText(INNER).performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription(string(R.string.transaction_add)).assertIsDisplayed()
    }

    @Test
    fun `update pill floats over the bar on tabs and hides on inner screens`() {
        val pill = string(R.string.update_pill).replace("%1\$d", "43")
        compose.onNodeWithText(pill).assertIsDisplayed()
        val bar = compose.onNodeWithContentDescription(string(R.string.transaction_add)).getBoundsInRoot()
        assertTrue("pill must sit above the bar", compose.onNodeWithText(pill).getBoundsInRoot().bottom <= bar.top)
        compose.onNodeWithText(pill).performClick()
        compose.waitForIdle()
        assertEquals(1, pillTaps)

        openSettings()
        compose.waitForIdle()
        compose.onNodeWithText(pill).assertDoesNotExist()
    }

    @Test
    fun `long press on a row swaps the tabs for the selection bar`() {
        tab(R.string.nav_transactions)
        compose.onNodeWithText(TRANSACTION_CATEGORY).performTouchInput { longClick() }
        compose.waitForIdle()
        compose.onNodeWithText(string(R.string.transactions_selected).replace("%d", "1")).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.transactions_delete_selected).replace("%d", "1")).assertIsDisplayed()
        compose.onNodeWithContentDescription(string(R.string.transaction_add)).assertDoesNotExist()
        // «Назад» leaves the mode (in this test the update pill covers «✕» at the top left).
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
        compose.onNodeWithText(string(R.string.nav_transactions)).assertIsDisplayed()
        compose.onNodeWithContentDescription(string(R.string.transaction_add)).assertIsDisplayed()
    }

    @Test
    fun `swipe flips tabs`() {
        compose.onRoot().performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.onNodeWithText(TRANSACTION_CATEGORY).assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeRight() }
        compose.waitForIdle()
        compose.onNodeWithText(HOME_ACCOUNT).assertIsDisplayed()
    }

    /** «Ещё» → «Настройки»: the row is below the tiles, so the grid scrolls to it first (it is lazy). */
    private fun openSettings() {
        tab(R.string.nav_more)
        val settings = string(R.string.settings_title)
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(settings))
        compose.onNodeWithText(settings).performClick()
    }

    private fun tab(@StringRes label: Int) {
        // Tabs are icons without text (mockups): found by the label TalkBack reads.
        compose.onNodeWithContentDescription(string(label)).performClick()
        compose.waitForIdle()
    }

    private fun string(@StringRes id: Int) = compose.activity.getString(id)

    private operator fun Dp.times(n: Int) = Dp(value * n)

    private operator fun Dp.div(n: Int) = Dp(value / n)

    private companion object {
        const val HOME_ACCOUNT = "Домашняя карта"
        const val TRANSACTION_CATEGORY = "Кафе"
        const val ACCOUNT = "Наличные"
        const val INNER = "Внутренний экран"

        val HOME = HomeUiState(
            totals = listOf("1 310,50 USD"),
            rateDate = LocalDate(2026, 11, 9),
            accounts = listOf(HomeUiState.AccountRow("1", HOME_ACCOUNT, "120 800,50 ₽", isNegative = false)),
        )
        val TRANSACTIONS = TransactionsUiState(
            days = listOf(
                Day(
                    LocalDate(2026, 11, 8),
                    RelativeDay.TODAY,
                    listOf(Row("1", Kind.EXPENSE, TRANSACTION_CATEGORY, "Карта", "кофе", "-4 500,00 $")),
                ),
            ),
        )
        private val ACCOUNT_ROW = AccountsUiState.Row("1", ACCOUNT, AccountType.CASH, "85 000,00 $", isNegative = false)
        val ACCOUNTS = AccountsUiState(
            active = listOf(ACCOUNT_ROW),
            groups = listOf(AccountsUiState.Group("TYPE:CASH", titleText = "Cash", rows = listOf(ACCOUNT_ROW), total = null, expanded = true)),
        )
    }
}
