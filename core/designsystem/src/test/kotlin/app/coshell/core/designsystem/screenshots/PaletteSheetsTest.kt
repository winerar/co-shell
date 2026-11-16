package app.coshell.core.designsystem.screenshots

import androidx.compose.material3.Surface
import app.coshell.core.designsystem.catalog.ChartsCatalog
import app.coshell.core.designsystem.catalog.ComponentsCatalog
import app.coshell.core.designsystem.catalog.TokensCatalog
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.CoShellTheme
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Contact sheets for #381 (`release-checklist.md` §4): the catalogs in every palette × light / dark. Not references:
 * the files go to `build/palette-sheets/` (a CI artifact `palette-sheets`), git keeps only Lime and one soft palette
 * (`CatalogScreenshotTest`). Look at them for states — pressed, chosen, off, error, loading — in each palette.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h2600dp-xhdpi")
class PaletteSheetsTest {
    @Test
    fun `catalogs in every palette and both themes`() {
        CoShellPalette.entries.forEach { palette ->
            listOf(false, true).forEach { dark ->
                val mode = if (dark) "dark" else "light"
                sheet("components_${palette.name.lowercase()}_$mode", palette, dark) { ComponentsCatalog() }
                sheet("charts_${palette.name.lowercase()}_$mode", palette, dark) { ChartsCatalog() }
                sheet("tokens_${palette.name.lowercase()}_$mode", palette, dark) { TokensCatalog() }
            }
        }
    }

    private fun sheet(name: String, palette: CoShellPalette, dark: Boolean, content: @androidx.compose.runtime.Composable () -> Unit) =
        captureRoboImage(
            "build/palette-sheets/$name.png",
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record),
        ) { CoShellTheme(darkTheme = dark, palette = palette) { Surface { content() } } }
}
