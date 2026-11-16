package app.coshell.core.designsystem.screenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import app.coshell.core.designsystem.components.ButtonTone
import app.coshell.core.designsystem.components.CoShellAmountField
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellChoicePill
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Components under stress (#378, `release-checklist.md` §2): 50+ character texts, a word without spaces, a
 * billion-sized amount, on 320 dp with font ×1.3 (dark) and on 411 dp (light). Rule: text ends in «…» or wraps,
 * nothing overlaps, amounts are not cut. The frame is tall so the whole sample is in the picture.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h1700dp-xhdpi")
class CatalogStressScreenshotTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `components stress light`() = shot("components_stress_light") { StressSample() }

    @Test @Config(qualifiers = "ru-w320dp-h1900dp-xhdpi")
    fun `components stress narrow dark`() = shot("components_stress_narrow_dark", dark = true, fontScale = 1.3f) { StressSample() }

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

private const val LONG = "Коммунальные платежи и обслуживание квартиры в Буэнос-Айресе"
private const val WORD = "Продуктыиздомашнегомагазинабезпробеловвообще"

@Composable
private fun StressSample() {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        CoShellSectionLabel(LONG)
        CoShellTextField(value = LONG, onValueChange = {}, label = LONG, error = "$LONG — слишком длинное название")
        CoShellAmountField(value = "999 999 999,99", onValueChange = {}, label = LONG, currency = "USDT", hint = LONG)
        CoShellHint(LONG)
        CoShellMessage(LONG, isError = true)
        CoShellButton(onClick = {}) { Text(LONG) }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            CoShellButton(onClick = {}) { Text("Сохранить изменения") }
            CoShellButton(onClick = {}, tone = ButtonTone.Tonal) { Text("Провести сейчас") }
        }
        CoShellSegmented(labels = listOf("Расходы и доходы", "Переводы между счетами", WORD), selected = 1, onSelect = {})
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            CoShellFilterChip(selected = true, onClick = {}, label = { Text(LONG) })
            CoShellFilterChip(selected = false, onClick = {}, label = { Text(WORD) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            CoShellChoicePill("Наличные", selected = true, onClick = {})
            CoShellChoicePill(WORD, selected = false, onClick = {})
        }
        CoShellCard {
            CoShellListRow(title = LONG, subtitle = "$LONG · $WORD", trailing = { Text("−999 999 999,99 USDT") })
            CoShellListRow(title = WORD, subtitle = LONG, subtitleMaxLines = 1, trailing = { Text("0") })
            CoShellSwitchRow(title = LONG, checked = true, onCheckedChange = {}, subtitle = "$LONG. $LONG")
        }
    }
}
