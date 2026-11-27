package app.coshell.home

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.settings.HomeCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** «Скрыто: N · Вернуть» in the editing mode of «Обзор» (#154): the way back for a hidden card. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp", application = Application::class)
class HiddenCardsBarTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val shown = mutableListOf<HomeCard>()

    private fun set(hidden: List<HomeCard>) = compose.setContent { CoShellTheme { HiddenCardsBar(hidden, onShow = { shown += it }) } }

    @Test
    fun `nothing hidden means no bar`() {
        set(emptyList())
        compose.onNodeWithText("Вернуть").assertDoesNotExist()
    }

    @Test
    fun `the bar counts the hidden cards and the sheet shows one back`() {
        set(listOf(HomeCard.CONVERTER, HomeCard.RECENT))
        compose.onNodeWithText("Скрыто: 2").assertIsDisplayed()
        compose.onNodeWithText("Вернуть").performClick()
        compose.onNodeWithText("Скрытые карточки").assertIsDisplayed()
        compose.onNodeWithText("Конвертер").assertIsDisplayed()
    }

    @Test
    fun `the show button gives the card back`() {
        set(listOf(HomeCard.CONVERTER))
        compose.onNodeWithText("Вернуть").performClick()
        compose.onNodeWithText("Показать").performClick()
        assertEquals(listOf(HomeCard.CONVERTER), shown)
    }
}
