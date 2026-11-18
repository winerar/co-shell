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
import androidx.compose.ui.unit.dp
import app.coshell.converter.ConverterActions
import app.coshell.converter.ConverterRow
import app.coshell.converter.ConverterSheetBody
import app.coshell.converter.ConverterUiState
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.settings.ConverterMode
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The converter sheet (#319, boards `Conv2PairType`, `Conv2AllType`): «Пара» and «Все валюты» with 100 USD typed, on the owner's phone (393×873 dp). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w393dp-h873dp-xhdpi", application = Application::class)
class ConverterScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `pair dark`() = shot("converter_pair_dark", dark = true) { ConverterSheetBody(PAIR, ConverterActions(), onClose = {}) }

    @Test
    fun `pair light`() = shot("converter_pair_light") { ConverterSheetBody(PAIR, ConverterActions(), onClose = {}) }

    @Test
    fun `all dark`() = shot("converter_all_dark", dark = true) { ConverterSheetBody(ALL, ConverterActions(), onClose = {}) }

    @Test
    fun `all light`() = shot("converter_all_light") { ConverterSheetBody(ALL, ConverterActions(), onClose = {}) }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { Column(Modifier.padding(top = 70.dp)) { content() } } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        const val NB = " " // grouping as MoneyFormat gives it
        val ROWS = listOf(
            ConverterRow("USD", "100", isSource = true),
            ConverterRow("AMD", "38${NB}700", isSource = false),
            ConverterRow("ARS", "101${NB}500", isSource = false),
            ConverterRow("RUB", null, isSource = false),
            ConverterRow("USDT", "100,10", isSource = false),
        )
        val PAIR = ConverterUiState(
            isLoading = false, from = "USD", to = "ARS", fromText = "100", toText = "101${NB}500", isEmpty = false,
            rate = "1${NB}015", rows = ROWS, options = ROWS.map { it.code }, source = "USD",
        )
        val ALL = PAIR.copy(mode = ConverterMode.ALL)
    }
}
