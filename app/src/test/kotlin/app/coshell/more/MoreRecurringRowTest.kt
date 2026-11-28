package app.coshell.more

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * «Ещё → Деньги → Регулярные платежи» (#403, owner's choice А): the screen stays reachable when «Скоро» on «Обзор»
 * is hidden because nothing is due within 7 days.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp", application = Application::class)
class MoreRecurringRowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `recurring payments tile opens the list without scrolling`() {
        var opened = 0
        compose.setContent { CoShellTheme { MoreScreen(MoreActions(onRecurring = { opened++ })) } }
        val title = compose.activity.getString(R.string.more_tile_recurring)
        compose.onNodeWithText(title).performClick()
        compose.waitForIdle()
        assertEquals(1, opened)
    }

    /** Poll 2026-11-28 «А»: the whole menu on one screen of a usual phone — the last row is seen without scrolling. */
    @Test
    fun `whole menu fits one screen`() {
        compose.setContent { CoShellTheme { MoreScreen(MoreActions(), MoreState("0.37", 400)) } }
        compose.onNodeWithText(compose.activity.getString(R.string.about_title)).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.drafts_title)).assertIsDisplayed()
    }
}
