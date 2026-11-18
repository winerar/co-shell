package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.budgets.detail.BudgetDetailActions
import app.coshell.budgets.detail.BudgetDetailScreen
import app.coshell.budgets.detail.budgetDetailPreviewSample
import app.coshell.budgets.edit.BudgetEditActions
import app.coshell.budgets.edit.BudgetEditScreen
import app.coshell.budgets.edit.BudgetEditUiState
import app.coshell.budgets.edit.BudgetKind
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.importer.ImportBatch
import app.coshell.core.model.MoneyFormat
import app.coshell.importer.wallet.WalletImportActions
import app.coshell.importer.wallet.WalletImportScreen
import app.coshell.importer.wallet.WalletImportUiState
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlin.time.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Бюджет · Дом» (board `BudDetail`), «Лимит» (`BudEdit`) and the start of «Импорт из Wallet» (`ImportStart`) in both themes (#464). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class BudgetImportScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `budget detail light`() = shot("budget_detail_light") { detail() }

    @Test fun `budget detail dark`() = shot("budget_detail_dark", dark = true) { detail() }

    @Test fun `budget edit light`() = shot("budget_edit_light") { edit() }

    @Test fun `budget edit dark`() = shot("budget_edit_dark", dark = true) { edit() }

    @Test fun `import start light`() = shot("import_start_light") { import() }

    @Test fun `import start dark`() = shot("import_start_dark", dark = true) { import() }

    @Composable private fun detail() = BudgetDetailScreen(budgetDetailPreviewSample, BudgetDetailActions(), categoryId = "home")

    @Composable private fun edit() = BudgetEditScreen(
        BudgetEditUiState(
            month = YearMonth(2026, 11),
            kind = BudgetKind.FOLDER,
            folders = listOf(BudgetEditUiState.Choice("home", "Дом", "home", 0xFF00BCD4)),
            categoryId = "home",
            amount = "400 000",
            currency = "ARS",
            currencies = listOf("ARS", "USD"),
            average = "372 000 ARS",
            inside = listOf(
                BudgetEditUiState.Inside("rent", "Аренда", "home", 0xFF00BCD4, null),
                BudgetEditUiState.Inside("utilities", "Коммуналка", "bolt", 0xFF00BCD4, "70 000 ARS"),
            ),
            note = "Аренда 280 000 до 5-го, свет и газ ~60 000",
            hasLimit = true,
        ),
        BudgetEditActions(),
    )

    @Composable private fun import() = WalletImportScreen(
        WalletImportUiState(
            batches = listOf(
                ImportBatch(
                    "b1", "wallet-2026-11.csv", 1_284,
                    Instant.parse("2024-01-03T00:00:00Z"), Instant.parse("2026-11-09T00:00:00Z"), Instant.parse("2026-11-10T12:00:00Z"),
                ),
            ),
        ),
        WalletImportActions(),
        MoneyFormat.RU,
        TimeZone.UTC,
    )

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
