package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A question too long for the screen (320 × 560 dp, font ×2): the text scrolls, both buttons stay whole and
 * visible — they were squashed like «Понятно» in «Обновлено до N» (owner's screenshot 2026-11-20).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w320dp-h560dp-xhdpi")
class ConfirmCardOverflowTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `long question keeps both buttons whole`() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                CoShellTheme(darkTheme = true) {
                    Surface {
                        Box(Modifier.fillMaxSize().padding(vertical = Spacing.l), contentAlignment = Alignment.Center) {
                            CoShellConfirmCard(
                                title = "Удалить счёт и все его операции?",
                                text = List(6) { "Счёт удалится навсегда вместе с операциями, шаблонами и лимитами." }.joinToString(" "),
                                confirmLabel = "Удалить",
                                onConfirm = {},
                                dismissLabel = "Отмена",
                                destructive = true,
                                modifier = Modifier.padding(horizontal = Spacing.l),
                            )
                        }
                    }
                }
            }
        }
        compose.onNodeWithText("Удалить").assertIsDisplayed().assertHeightIsAtLeast(Sizes.button)
        compose.onNodeWithText("Отмена").assertIsDisplayed().assertHeightIsAtLeast(Sizes.button)
        compose.onRoot().captureRoboImage("src/test/screenshots/confirm_overflow_x2_dark.png")
    }
}
