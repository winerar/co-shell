package app.coshell.navigation

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.coshell.R
import app.coshell.accounts.list.AccountsScreen
import app.coshell.accounts.list.AccountsUiState
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.model.AccountType
import app.coshell.home.HomeScreen
import app.coshell.home.HomeUiState
import app.coshell.more.MoreActions
import app.coshell.more.MoreScreen
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

    @Before
    fun setUp() {
        compose.setContent {
            CoShellTheme(darkTheme = true) {
                MainShell(rememberNavController(), SnackbarHostState(), onAdd = { adds++ }) {
                    composable<HomeDestination> { HomeScreen(HOME) }
                    composable<TransactionsDestination> { TransactionsScreen(TRANSACTIONS) }
                    composable<AccountsDestination> { AccountsScreen(ACCOUNTS, {}, {}, {}) }
                    composable<MoreDestination> { MoreScreen(MoreActions()) }
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

    private fun tab(@StringRes label: Int) {
        compose.onNodeWithText(string(label)).performClick()
        compose.waitForIdle()
    }

    private fun string(@StringRes id: Int) = compose.activity.getString(id)

    private operator fun Dp.times(n: Int) = Dp(value * n)

    private operator fun Dp.div(n: Int) = Dp(value / n)

    private companion object {
        const val HOME_ACCOUNT = "Домашняя карта"
        const val TRANSACTION_CATEGORY = "Кафе"
        const val ACCOUNT = "Наличные"

        val HOME = HomeUiState(
            totals = listOf("1 310,50 USD"),
            rateDate = LocalDate(2026, 11, 9),
            byCurrency = listOf("150,00 USD"),
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
        val ACCOUNTS = AccountsUiState(
            active = listOf(AccountsUiState.Row("1", ACCOUNT, AccountType.CASH, "85 000,00 $", isNegative = false)),
        )
    }
}
