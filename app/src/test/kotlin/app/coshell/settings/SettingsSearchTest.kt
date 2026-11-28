package app.coshell.settings

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** «Найти настройку» (poll 2026-11-28 «А»): how a query meets the names, and a found setting opens its screen. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp", application = Application::class)
class SettingsSearchTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `query matches inside words regardless of case and yo`() {
        assertTrue(SettingsMatch.matches("ВИБР", "Вибрация"))
        assertTrue(SettingsMatch.matches("темная", "Тёмная тема"))
        assertTrue(SettingsMatch.matches("копия рез", "Резервная копия"))
        assertFalse(SettingsMatch.matches("копия wifi", "Резервная копия"))
        assertFalse(SettingsMatch.matches("  ", "Тема"))
    }

    @Test
    fun `names come before the screens they are on`() {
        val entries = listOf("Тема" to "Внешний вид", "Палитра" to "Внешний вид", "Вид строки" to "Операции")
        val found = SettingsMatch.search("вид", entries, { it.first }, { it.second })
        assertEquals(listOf("Вид строки" to "Операции", "Тема" to "Внешний вид", "Палитра" to "Внешний вид"), found)
    }

    @Test
    fun `every group of the index is reachable from the search`() {
        val reached = SETTINGS_ENTRIES.map { it.target }.toSet()
        val groups = setOf(SettingsTarget.LOOK, SettingsTarget.INPUT, SettingsTarget.NOTICES, SettingsTarget.PRIVACY, SettingsTarget.HELP, SettingsTarget.MONEY, SettingsTarget.DATA)
        assertTrue((groups - reached).toString(), reached.containsAll(groups))
    }

    @Test
    fun `found setting opens its screen`() {
        val opened = mutableListOf<SettingsTarget>()
        compose.setContent { CoShellTheme { SettingsScreen(SettingsState(), SettingsActions(onOpen = { opened += it }), initialQuery = "вибр") } }
        compose.onNodeWithText(compose.activity.getString(R.string.settings_input_haptics)).performClick()
        compose.waitForIdle()
        assertEquals(listOf(SettingsTarget.INPUT), opened)
    }

    @Test
    fun `nothing found says so`() {
        compose.setContent { CoShellTheme { SettingsScreen(SettingsState(), SettingsActions(), initialQuery = "абракадабра") } }
        compose.onNodeWithText(compose.activity.getString(R.string.settings_search_none)).assertExists()
    }

    @Test
    fun `index opens the group`() {
        val opened = mutableListOf<SettingsTarget>()
        compose.setContent { CoShellTheme { SettingsScreen(SettingsState(), SettingsActions(onOpen = { opened += it })) } }
        compose.onNodeWithText(compose.activity.getString(R.string.privacy_title)).performClick()
        compose.waitForIdle()
        assertEquals(listOf(SettingsTarget.PRIVACY), opened)
    }
}
