package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditScreen
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.categoryChoiceOptions
import app.coshell.ui.form.ChoiceRows
import app.coshell.transactions.edit.components.TipSheetBody
import app.coshell.transactions.edit.entrySheetSample
import app.coshell.transactions.edit.tip.TipUi
import app.coshell.transactions.edit.tipSheetSample
import app.coshell.transactions.edit.paymentSheetSample
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The entry sheet without scrolling (#290, board `Fb290A`) on the owner's phone (393×873 dp), under the room the status
 * bar, the sheet's top margin and its handle take (≈102 dp) — «Детали» must end above the keypad; and «Все категории»
 * (board `Fb290ACats`); the «Провести» sheet of a payment (#404, board `Rec143Post`).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w393dp-h873dp-xhdpi", application = Application::class)
class EntrySheetScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `entry sheet dark`() = shot("entry_sheet_dark", dark = true) { sheet() }

    @Test
    fun `entry sheet light`() = shot("entry_sheet_light") { sheet() }

    @Test
    fun `payment sheet dark`() = shot("recurring_post_dark", dark = true) { sheet(paymentSheetSample) }

    @Test
    fun `payment sheet light`() = shot("recurring_post_light") { sheet(paymentSheetSample) }

    @Test
    fun `all categories dark`() = shot("entry_all_categories_dark", dark = true) { allCategories() }

    @Test
    fun `all categories light`() = shot("entry_all_categories_light") { allCategories() }

    @Test
    fun `tip dark`() = shot("entry_tip_dark", dark = true) { tip() }

    @Test
    fun `tip light`() = shot("entry_tip_light") { tip() }

    @Test
    fun `tip own amount dark`() = shot("entry_tip_own_dark", dark = true) { tip(tipSheetSample.copy(percent = null, own = "500", tipText = "500 ARS")) }

    @Test
    fun `tip own amount light`() = shot("entry_tip_own_light") { tip(tipSheetSample.copy(percent = null, own = "500", tipText = "500 ARS")) }

    @Composable
    private fun tip(tip: TipUi = tipSheetSample) = Column(Modifier.padding(horizontal = Spacing.l).padding(top = SHEET_TOP)) {
        TipSheetBody(entrySheetSample, tip, TransactionEditActions())
    }

    @Composable
    private fun sheet(state: TransactionEditUiState = entrySheetSample) =
        Column(Modifier.padding(top = SHEET_TOP)) { TransactionEditScreen(state, TransactionEditActions()) }

    @Composable
    private fun allCategories() = Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        ChoiceRows(
            options = categoryChoiceOptions(entrySheetSample.categories, "Без группы"),
            selected = entrySheetSample.categoryId,
            query = "",
            onQueryChange = {},
            showSearch = true,
            searchPlaceholder = "Найти категорию",
            onPick = {},
        )
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
