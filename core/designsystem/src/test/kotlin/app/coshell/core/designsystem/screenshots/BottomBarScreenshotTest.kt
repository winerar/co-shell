package app.coshell.core.designsystem.screenshots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.components.CoShellBottomBar
import app.coshell.core.designsystem.components.CoShellTab
import app.coshell.core.designsystem.theme.CoShellTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** #796: every tab selected on a normal phone — the pill names the tab under its icon, «+» stays in the middle. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h300dp-xhdpi")
class BottomBarScreenshotTest {
    @Test
    fun `bar operations selected dark`() = shot("bottom_bar_ops_dark", selected = 1, dark = true)

    @Test
    fun `bar reports selected light`() = shot("bottom_bar_reports_light", selected = 2)

    @Test
    fun `bar more selected dark`() = shot("bottom_bar_more_dark", selected = 3, dark = true)

    private fun shot(name: String, selected: Int, dark: Boolean = false) = shotBar(name, selected, dark, 1f)
}

/** #796, 320 dp and a large font: a name that does not fit is hidden (only the icon stays), nothing is cut or overlaps. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w320dp-h300dp-xhdpi")
class BottomBarStressScreenshotTest {
    @Test
    fun `bar operations selected narrow x2 dark`() = shotBar("bottom_bar_stress_ops_dark", 1, dark = true, fontScale = 2f)

    @Test
    fun `bar overview selected narrow x13 light`() = shotBar("bottom_bar_stress_home_light", 0, dark = false, fontScale = 1.3f)
}

private fun shotBar(name: String, selected: Int, dark: Boolean, fontScale: Float) =
    captureRoboImage("src/test/screenshots/$name.png") {
        CoShellTheme(darkTheme = dark) {
            Surface {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    Column(Modifier.height(120.dp)) { Bar(selected) }
                }
            }
        }
    }

@Composable
private fun Bar(selected: Int) {
    val tabs = listOf(
        CoShellTab(R.drawable.ic_home, "Обзор", selected == 0),
        CoShellTab(R.drawable.ic_receipt_long, "Операции", selected == 1),
        CoShellTab(R.drawable.ic_bar_chart, "Отчёты", selected == 2),
        CoShellTab(R.drawable.ic_more_horiz, "Ещё", selected == 3),
    )
    CoShellBottomBar(tabs = tabs, onTab = {}, addLabel = "Новая операция", onAdd = {})
}
