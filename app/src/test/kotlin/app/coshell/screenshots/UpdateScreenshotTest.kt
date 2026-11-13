package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.update.AppRelease
import app.coshell.core.domain.update.ReleaseNote
import app.coshell.update.UpdateActions
import app.coshell.update.UpdatePhase
import app.coshell.update.UpdateScreen
import app.coshell.update.UpdateUiState
import app.coshell.update.past.PastReleasesScreen
import app.coshell.update.past.PastReleasesState
import com.github.takahirom.roborazzi.captureRoboImage
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Обновление приложения» (#268, boards Fb268A, Fb268B, Fb269A) and «Прошлые версии» (#284, Fb284A); the same setup as [ScreensScreenshotTest]. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class UpdateScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `update available dark`() = shot("update_available_dark", dark = true) {
        UpdateScreen(installed.copy(phase = UpdatePhase.Available(next)), UpdateActions())
    }

    @Test
    fun `update latest light`() = shot("update_latest_light") {
        UpdateScreen(installed.copy(phase = UpdatePhase.UpToDate(current)), UpdateActions())
    }

    @Test
    fun `update installing dark`() = shot("update_installing_dark", dark = true) {
        UpdateScreen(installed.copy(phase = UpdatePhase.Installing(next)), UpdateActions())
    }

    @Test
    fun `past releases dark`() = shot("update_past_dark", dark = true) {
        PastReleasesScreen(PastReleasesState.Loaded(past, 400), onBack = {}, onRetry = {})
    }

    @Test
    fun `past releases light`() = shot("update_past_light") {
        PastReleasesScreen(PastReleasesState.Loaded(past, 400), onBack = {}, onRetry = {})
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        val installed = UpdateUiState("0.37", 400, installedAt = Instant.parse("2026-11-12T12:00:00Z"))
        private const val NOTES = "• Плавная смена темы, когда телефон переключает светлую и тёмную\n" +
            "• Поиск по операциям — пилюлей сверху\n• Приложение легче: 8,0 → 5,5 МБ"
        val current = AppRelease(400, "0.37", "https://x/co-shell-400.apk", "0".repeat(64), 5_800_000, NOTES)
        /** 12 releases: the list must stay short with many (board Fb284A). */
        val past = (0 until 12).map { i ->
            ReleaseNote(400 - i * 4, "0.${37 - i}", LocalDate(2026, 11, 12).minus(DatePeriod(days = i * 2)), if (i % 3 == 1) "• Исправления" else NOTES)
        }
        val next = AppRelease(404, "0.38", "https://x/co-shell-404.apk", "0".repeat(64), 5_800_000, NOTES)
    }
}
