package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.budgets.DailyBase
import app.coshell.home.daily.DailyCard
import app.coshell.home.daily.DailyUi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Сегодня можно» (#769; boards DailyHome, DailyOver): the card on «Обзоре», in the plan and over it. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h500dp-xhdpi", application = Application::class)
class DailyScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val inPlan = DailyUi(
        perDay = "9 800 ARS", days = 5, left = "49 000 ARS", over = false, spentToday = "2 450 ARS", todayFraction = 0.25f,
        base = DailyBase.LIMITS, month = "ноябрь", baseAmount = "600 000 ARS", spent = "451 000 ARS", upcoming = "65 000 ARS", goals = "35 000 ARS", missing = "",
    )
    private val over = inPlan.copy(perDay = "0 ARS", left = "12 000 ARS", over = true, todayFraction = 1f)

    @Test fun `daily dark`() = shot("daily_dark", dark = true) { DailyCard(inPlan) }

    @Test fun `daily light`() = shot("daily_light") { DailyCard(inPlan) }

    @Test fun `daily over dark`() = shot("daily_over_dark", dark = true) { DailyCard(over) }

    @Test fun `daily over light`() = shot("daily_over_light") { DailyCard(over) }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { Column(Modifier.padding(Spacing.l)) { content() } } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
