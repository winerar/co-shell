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
import app.coshell.core.designsystem.components.CoShellModalSheetLayout
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionType
import app.coshell.templates.edit.TemplateEditActions
import app.coshell.templates.edit.TemplateEditScreen
import app.coshell.templates.edit.TemplateEditUiState
import app.coshell.templates.edit.components.RepeatDraft
import app.coshell.templates.edit.components.RepeatSheetBody
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Повтор» in the template (#143, boards `Rec143EditA`, `Rec143RepeatA`) on the owner's phone (393×873 dp). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w393dp-h873dp-xhdpi", application = Application::class)
class RecurringScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `template with a repeat dark`() = shot("recurring_template_dark", dark = true) { template() }

    @Test
    fun `template with a repeat light`() = shot("recurring_template_light") { template() }

    @Test
    fun `repeat sheet dark`() = shot("recurring_sheet_dark", dark = true) { sheet() }

    @Test
    fun `repeat sheet light`() = shot("recurring_sheet_light") { sheet() }

    @Composable
    private fun template() = TemplateEditScreen(STATE, TemplateEditActions())

    /** The tallest frequency: a day grid of 31 plus both modes. */
    @Composable
    private fun sheet() = Column(Modifier.padding(top = 102.dp)) {
        CoShellModalSheetLayout(title = "Повтор", closeLabel = "", onClose = {}, subtitle = "Аренда · 650 USD", actionLabel = "Готово") {
            RepeatSheetBody(RepeatDraft.from(Recurrence.Monthly(5), PostMode.REMIND, TODAY), STATE, onChange = {})
        }
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        val TODAY = LocalDate(2026, 11, 15)
        val STATE = TemplateEditUiState(
            isNew = false,
            type = TransactionType.EXPENSE,
            name = "Аренда",
            accounts = listOf(TemplateEditUiState.AccountChoice("cash", "Наличные", "USD")),
            accountId = "cash",
            amount = "650",
            note = "Хозяйке, налом",
            today = TODAY,
            schedule = RecurringSchedule(Recurrence.Monthly(5), TODAY),
        )
    }
}
