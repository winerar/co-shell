package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsSamples
import app.coshell.reports.components.IncomeCard
import app.coshell.reports.components.MembersCard
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Картина целиком» (#189): the new cards of «Отчёты» on [ReportsSamples] — the numbers of boards `Rp189*`. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class ReportsPictureScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `picture dark`() = shot("reports_picture_dark", dark = true) { cards() }

    @Test
    fun `picture light`() = shot("reports_picture_light") { cards() }

    @Test @Config(qualifiers = "+en")
    fun `picture english`() = shot("reports_picture_en", dark = true) { cards() }

    @Composable
    private fun cards() = Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        IncomeCard(ReportsSamples.income, onMonth = {})
        MembersCard(ReportsSamples.members)
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(SETTLE_MS)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        const val SETTLE_MS = 3_000L
    }
}
