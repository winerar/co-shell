package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.recurring.PaymentState
import app.coshell.core.model.Recurrence
import app.coshell.core.model.TransactionType
import app.coshell.recurring.list.MonthAheadUi
import app.coshell.recurring.list.PaymentFilter
import app.coshell.recurring.list.RecurringListActions
import app.coshell.recurring.list.RecurringListScreen
import app.coshell.recurring.list.RecurringListUiState
import app.coshell.recurring.list.RecurringMonth
import app.coshell.recurring.list.RecurringRow
import app.coshell.templates.TemplateItem
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Регулярные платежи» (#403, board `Rec143List`) on the owner's phone (393×873 dp); today — 15 November 2026. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w393dp-h873dp-xhdpi", application = Application::class)
class RecurringListScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `list dark`() = shot("recurring_list_dark", dark = true) { RecurringListScreen(STATE, RecurringListActions()) }

    @Test
    fun `list light`() = shot("recurring_list_light") { RecurringListScreen(STATE, RecurringListActions()) }

    @Test
    fun `empty dark`() = shot("recurring_list_empty_dark", dark = true) {
        RecurringListScreen(RecurringListUiState(), RecurringListActions())
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        val NOVEMBER = YearMonth(2026, Month.NOVEMBER)

        @Suppress("LongParameterList")
        fun row(
            name: String,
            icon: String,
            color: Int,
            amount: String,
            currency: String,
            account: String,
            recurrence: Recurrence,
            date: LocalDate,
            state: PaymentState = PaymentState.SOON,
            type: TransactionType = TransactionType.EXPENSE,
            auto: Boolean = false,
        ) = RecurringRow(
            TemplateItem(name, name, type, account, categoryName = icon, categoryIcon = icon, categoryColor = CategoryPalette.colors[color],
                amount = if (amount.isEmpty()) "" else "$amount $currency", currency = currency),
            recurrence, auto, date, state, missed = if (state == PaymentState.MISSED) 1 else 0,
        )

        fun nov(day: Int) = LocalDate(2026, 11, day)
        fun dec(day: Int) = LocalDate(2026, 12, day)

        /** Board `Rec143List`: 24 rules, November by date, then December. */
        val STATE = RecurringListUiState(
            total = 24,
            counts = mapOf(PaymentFilter.ALL to 24, PaymentFilter.EXPENSE to 20, PaymentFilter.INCOME to 4, PaymentFilter.AUTO to 7),
            ahead = MonthAheadUi(NOVEMBER, count = 9, total = "−410 USD", withoutAmount = 3),
            months = listOf(
                RecurringMonth(NOVEMBER, listOf(
                    row("Уборка", "cleaning", 2, "30 000", "ARS", "Наличные", Recurrence.Weekly(DayOfWeek.FRIDAY), nov(13), PaymentState.MISSED),
                    row("Свет", "utilities", 4, "", "ARS", "Galicia", Recurrence.Monthly(15), nov(15), PaymentState.TODAY),
                    row("Испанский", "education", 6, "40 000", "ARS", "Galicia", Recurrence.Weekly(DayOfWeek.TUESDAY), nov(17)),
                    row("Netflix", "subscriptions", 8, "7 990", "ARS", "USDT-карта", Recurrence.Monthly(18), nov(18), auto = true),
                    row("Перевод маме", "gift", 10, "50 000", "AMD", "Ameria", Recurrence.Monthly(20), nov(20)),
                    row("Газ", "utilities", 4, "", "ARS", "Galicia", Recurrence.Monthly(22), nov(22)),
                    row("Spotify", "subscriptions", 8, "3 490", "ARS", "USDT-карта", Recurrence.Monthly(24), nov(24), auto = true),
                )),
                RecurringMonth(YearMonth(2026, Month.DECEMBER), listOf(
                    row("Зарплата", "salary", 12, "1 200", "USD", "Ameria", Recurrence.Monthly(1), dec(1), type = TransactionType.INCOME),
                    row("Аренда", "home", 14, "650", "USD", "Наличные", Recurrence.Monthly(5), dec(5)),
                    row("Expensas", "home", 2, "180 000", "ARS", "Galicia", Recurrence.Monthly(10), dec(10)),
                )),
            ),
        )
    }
}
