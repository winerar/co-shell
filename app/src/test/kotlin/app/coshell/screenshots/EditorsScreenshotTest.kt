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
import app.coshell.accounts.edit.AccountEditActions
import app.coshell.accounts.edit.AccountEditScreen
import app.coshell.accounts.edit.accountEditPreviewSample
import app.coshell.categories.edit.CategoryEditActions
import app.coshell.categories.edit.CategoryEditScreen
import app.coshell.categories.edit.categoryEditPreviewSample
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.AccountType
import app.coshell.ui.form.IconColorContent
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Editors without scrolling (#290, boards `Fb290FA`, `Fb290Icons`) on the owner's phone (393×873 dp) under the sheet's
 * top (≈102 dp): «Сохранить» must be on screen with the whole form above it.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w393dp-h873dp-xhdpi", application = Application::class)
class EditorsScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `category editor dark`() = shot("editor_category_dark", dark = true) { category() }

    @Test
    fun `category editor light`() = shot("editor_category_light") { category() }

    @Test
    fun `account editor dark`() = shot("editor_account_dark", dark = true) { account() }

    @Test
    fun `account editor light`() = shot("editor_account_light") { account() }

    @Test
    fun `icon and colour sheet dark`() = shot("editor_icon_color_dark", dark = true) { iconColor() }

    @Test
    fun `icon and colour sheet light`() = shot("editor_icon_color_light") { iconColor() }

    @Composable
    private fun category() = Column(Modifier.padding(top = SHEET_TOP)) { CategoryEditScreen(categoryEditPreviewSample, CategoryEditActions()) }

    /** The tallest account form: both switches (a family) and «Как у Galicia» on the colour card. */
    @Composable
    private fun account() = Column(Modifier.padding(top = SHEET_TOP)) {
        AccountEditScreen(
            accountEditPreviewSample.copy(name = "Galicia · карта", type = AccountType.CARD, showSharing = true, color = null),
            AccountEditActions(),
        )
    }

    @Composable
    private fun iconColor() = Column(Modifier.padding(Spacing.l)) {
        IconColorContent(categoryEditPreviewSample.icon, categoryEditPreviewSample.color, onIconSelect = {}, onColorSelect = {})
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        val SHEET_TOP = 102.dp
    }
}
