package app.coshell.more

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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
    fun `recurring payments row opens the list`() {
        var opened = 0
        compose.setContent { CoShellTheme { MoreScreen(MoreActions(onRecurring = { opened++ })) } }
        val title = compose.activity.getString(R.string.recurring_list_title)
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(title))
        compose.onNodeWithText(compose.activity.getString(R.string.recurring_menu_hint)).assertExists()
        compose.onNodeWithText(title).performClick()
        compose.waitForIdle()
        assertEquals(1, opened)
    }
}
