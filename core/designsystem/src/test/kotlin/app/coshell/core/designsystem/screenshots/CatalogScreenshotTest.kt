package app.coshell.core.designsystem.screenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.catalog.ComponentsCatalog
import app.coshell.core.designsystem.catalog.TokensCatalog
import app.coshell.core.designsystem.components.CoShellBottomBar
import app.coshell.core.designsystem.components.CoShellPaletteSwatch
import app.coshell.core.designsystem.components.CoShellTab
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot references of the design system (#225): the catalogs in light and dark theme. A diff here
 * means the look changed — on purpose → `scripts/agent/agent.sh shots` and show the new PNG to the owner;
 * by accident → fix the code. References: `core/designsystem/src/test/screenshots/`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi")
class CatalogScreenshotTest {
    @Test
    fun `components light`() = shot("components_light") { ComponentsCatalog() }

    @Test
    fun `components dark`() = shot("components_dark", dark = true) { ComponentsCatalog() }

    @Test
    fun `tokens light`() = shot("tokens_light") { TokensCatalog() }

    @Test
    fun `tokens dark`() = shot("tokens_dark", dark = true) { TokensCatalog() }

    @Test
    fun `bottom bar dark`() = shot("bottom_bar_dark", dark = true) {
        CoShellBottomBar(
            tabs = listOf(
                CoShellTab(R.drawable.ic_home, "Обзор", selected = true),
                CoShellTab(R.drawable.ic_receipt_long, "Операции", selected = false),
                CoShellTab(R.drawable.ic_account_balance_wallet, "Счета", selected = false),
                CoShellTab(R.drawable.ic_more_horiz, "Ещё", selected = false),
            ),
            onTab = {},
            addLabel = "Новая операция",
            onAdd = {},
        )
    }

    @Test
    fun `palettes light`() = shot("palettes_light") {
        Row(Modifier.padding(Spacing.l), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            CoShellPalette.entries.forEach { palette ->
                CoShellPaletteSwatch(palette, palette.name, selected = palette == CoShellPalette.Volt, onClick = {})
            }
        }
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) =
        captureRoboImage("src/test/screenshots/$name.png") {
            CoShellTheme(darkTheme = dark) { Surface { content() } }
        }
}
