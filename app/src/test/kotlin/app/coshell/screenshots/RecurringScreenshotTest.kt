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
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.domain.recurring.PaymentState
import app.coshell.core.model.PostMode
import app.coshell.home.HomeScreen
import app.coshell.home.homePreviewSample
import app.coshell.home.upcoming.UpcomingRow
import app.coshell.templates.TemplateItem
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

/** Regular payments (#143, boards `Rec143HomeA`, `Rec143EditA`, `Rec143RepeatA`): «Скоро» on «Обзоре», «Повтор» in the template, on the owner's phone (393×873 dp). */
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

    @Test
    fun `home soon dark`() = shot("recurring_home_dark", dark = true) { HomeScreen(homePreviewSample, upcoming = SOON) }

    @Test
    fun `home soon light`() = shot("recurring_home_light") { HomeScreen(homePreviewSample, upcoming = SOON) }

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

        private fun row(name: String, cat: String, icon: String, color: Int, amount: String, date: LocalDate, state: PaymentState, auto: Boolean = false) =
            UpcomingRow(
                TemplateItem(name, name, TransactionType.EXPENSE, "Galicia", categoryName = cat, categoryIcon = icon,
                    categoryColor = CategoryPalette.colors[color], amount = amount, currency = "ARS"),
                date, state, missed = if (state == PaymentState.MISSED) 1 else 0, auto = auto,
            )

        /** Board `Rec143HomeA`: missed, today without an amount, next Tuesday, «само». */
        val SOON = listOf(
            row("Уборка", "Дом", "cleaning", 2, "30 000 ARS", LocalDate(2026, 11, 13), PaymentState.MISSED),
            row("Свет", "Коммуналка", "utilities", 4, "", TODAY, PaymentState.TODAY),
            row("Испанский", "Образование", "education", 6, "40 000 ARS", LocalDate(2026, 11, 17), PaymentState.SOON),
            row("Netflix", "Подписки", "subscriptions", 8, "7 990 ARS", LocalDate(2026, 11, 18), PaymentState.SOON, auto = true),
        )
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
