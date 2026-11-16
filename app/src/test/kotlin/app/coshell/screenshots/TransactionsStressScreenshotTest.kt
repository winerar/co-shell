package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.transactions.list.TransactionsScreen
import app.coshell.transactions.list.transactionsStressSample
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The operations list under stress (#378): long names, huge amounts, a day net in four currencies. The day title must
 * stay one line and the net wrap after a «·»; titles and accounts end in «…». Also at 320 dp with font ×1.3.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class TransactionsStressScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `transactions long texts light`() = shot("transactions_stress_light") { TransactionsScreen(transactionsStressSample) }

    @Test
    @Config(qualifiers = "ru-w320dp-h720dp-xhdpi")
    fun `transactions long texts narrow large font dark`() = shot("transactions_stress_narrow_dark", dark = true, fontScale = 1.3f) {
        TransactionsScreen(transactionsStressSample)
    }

    private fun shot(name: String, dark: Boolean = false, fontScale: Float = 1f, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                CoShellTheme(darkTheme = dark) { Surface { content() } }
            }
        }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
