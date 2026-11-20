package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.update.AppRelease
import app.coshell.update.notice.UpdatedCard
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * «Обновлено до N» with a long «Что нового» that does not fit the screen: the points scroll and «Понятно»
 * stays a whole button at the bottom (it was squashed into a thin strip, owner's screenshot 2026-11-20).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w390dp-h844dp-xhdpi", application = Application::class)
class UpdatedCardStressScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `updated long light`() = shot("updated_long_light", dark = false, fontScale = 1f)

    @Test @Config(qualifiers = "ru-w320dp-h640dp-xhdpi")
    fun `updated long x2 narrow dark`() = shot("updated_long_x2_narrow_dark", dark = true, fontScale = 2f)

    @Test fun `updated short light`() = shot("updated_short_light", dark = false, fontScale = 1f, notes = SHORT)

    private fun shot(name: String, dark: Boolean, fontScale: Float, notes: String = LONG_NOTES) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                CoShellTheme(darkTheme = dark) {
                    Surface {
                        Box(Modifier.fillMaxSize().padding(vertical = Spacing.l), contentAlignment = Alignment.Center) {
                            UpdatedCard(release(notes), onDismiss = {}, modifier = Modifier.padding(horizontal = Spacing.l + Spacing.xs))
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(3_000L)
        val ok = compose.activity.getString(R.string.update_whats_new_ok)
        compose.onNodeWithText(ok).assertIsDisplayed().assertHeightIsAtLeast(Sizes.button)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        val SHORT = "• Исправлено: знак «≈» больше не отрывается от суммы при переносе строки."
        val LONG_NOTES = listOf(
            "Исправлено: в светлой теме под выбранной вкладкой внизу больше нет лишней чёрточки.",
            "Телефоны с кнопками навигации внизу: под кнопками больше не просвечивает список, а нижняя панель стоит ниже — экрану достаётся больше места.",
            "Мягкие подсказки для тех, кто только начинает: короткие советы в шторке ввода, на тегах и валюте покупки, тёплые плашки за первую неделю записей и в конце месяца. Выключить или показать заново — «Ещё → Приложение».",
            "Исправлено: значок переворота на карточке «Бюджетов» больше не налезает на текст.",
            "Исправлено: в конвертере клавиатура остаётся на месте, а длинный список валют прокручивается над ней.",
            "Исправлено: знак «≈» больше не отрывается от суммы при переносе строки.",
            "Сканирование QR на чеке: сумма и дата подставятся сами.",
        ).joinToString("\n") { "• $it" }

        fun release(notes: String) = AppRelease(500, "1.3", "https://x/co-shell-500.apk", "0".repeat(64), 5_800_000, notes)
    }
}
