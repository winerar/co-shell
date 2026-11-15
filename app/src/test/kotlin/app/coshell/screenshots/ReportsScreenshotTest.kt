package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.reports.ReportsActions
import app.coshell.reports.ReportsSamples
import app.coshell.reports.ReportsScreen
import app.coshell.reports.all.ReportCategoriesScreen
import app.coshell.reports.category.ReportCategorySamples
import app.coshell.reports.category.ReportCategoryScreen
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Отчёты» (#140) on [ReportsSamples] — the numbers of board `RepA`: the tab, empty period, «Все категории» (`RepAll`), «Из чего сложилось» (`RepDrill`, #188). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class ReportsScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `reports dark`() = shot("reports_dark", dark = true) { ReportsScreen(ReportsSamples.state, ReportsActions()) }

    @Test
    fun `reports light`() = shot("reports_light") { ReportsScreen(ReportsSamples.state, ReportsActions()) }

    @Test @Config(qualifiers = "+en")
    fun `reports english`() = shot("reports_en", dark = true) { ReportsScreen(ReportsSamples.state, ReportsActions()) }

    @Test @Config(qualifiers = "+es")
    fun `reports spanish`() = shot("reports_es", dark = true) { ReportsScreen(ReportsSamples.state, ReportsActions()) }

    @Test
    fun `reports empty period`() = shot("reports_empty", dark = true) { ReportsScreen(ReportsSamples.empty, ReportsActions()) }

    @Test
    fun `all categories`() = shot("report_categories_dark", dark = true) { ReportCategoriesScreen(ReportsSamples.state, onBack = {}) }

    @Test
    fun `category drill dark`() = shot("report_category_dark", dark = true) { drill() }

    @Test
    fun `category drill light`() = shot("report_category_light") { drill() }

    @Composable
    private fun drill() = ReportCategoryScreen(ReportCategorySamples.state, onBack = {}, onTransactionClick = {})

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
