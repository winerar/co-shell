package app.coshell.core.designsystem.screenshots

import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.components.CoShellMenuRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.catalog.ComponentsCatalog
import app.coshell.core.designsystem.catalog.SegmentedSample
import app.coshell.core.designsystem.catalog.TokensCatalog
import app.coshell.core.designsystem.components.CoShellBottomBar
import app.coshell.core.designsystem.components.CoShellPaletteSwatch
import app.coshell.core.designsystem.components.CoShellTab
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.Column
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.CoShellSectionLabel
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot references of the design system (#225): the catalogs in light and dark theme. A diff here
 * means the look changed — on purpose → `scripts/agent/agent.sh shots` and show the new PNG to the owner;
 * by accident → fix the code. References: `core/designsystem/src/test/screenshots/`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi")
class CatalogScreenshotTest {
    @Test
    fun `components light`() = shot("components_light") { ComponentsCatalog() }

    @Test
    fun `components dark`() = shot("components_dark", dark = true) { ComponentsCatalog() }

    @Test
    fun `tokens light`() = shot("tokens_light") { TokensCatalog() }

    @Test
    fun `tokens dark`() = shot("tokens_dark", dark = true) { TokensCatalog() }

    @Test
    fun `bottom bar dark`() = shot("bottom_bar_dark", dark = true) {
        CoShellBottomBar(
            tabs = listOf(
                CoShellTab(R.drawable.ic_home, "Обзор", selected = true),
                CoShellTab(R.drawable.ic_receipt_long, "Операции", selected = false),
                CoShellTab(R.drawable.ic_account_balance_wallet, "Счета", selected = false),
                CoShellTab(R.drawable.ic_more_horiz, "Ещё", selected = false),
            ),
            onTab = {},
            addLabel = "Новая операция",
            onAdd = {},
        )
    }

    @Test
    fun `segmented dark`() = shot("segmented_dark", dark = true) { Column(Modifier.padding(Spacing.l)) { SegmentedSample() } }

    @Test
    fun `segmented light`() = shot("segmented_light") { Column(Modifier.padding(Spacing.l)) { SegmentedSample() } }

    @Test
    fun `row group dark`() = shot("row_group_dark", dark = true) { RowGroupSample() }

    @Test
    fun `row group light`() = shot("row_group_light") { RowGroupSample() }

    @Test
    fun `menu rows dark`() = shot("menu_rows_dark", dark = true) { MenuRowsSample() }

    @Test
    fun `menu rows light`() = shot("menu_rows_light") { MenuRowsSample() }

    @Test
    fun `palettes light`() = shot("palettes_light") {
        Row(Modifier.padding(Spacing.l), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            CoShellPalette.entries.forEach { palette ->
                CoShellPaletteSwatch(palette, palette.name, selected = palette == CoShellPalette.Volt, onClick = {})
            }
        }
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) =
        captureRoboImage("src/test/screenshots/$name.png") {
            CoShellTheme(darkTheme = dark) { Surface { content() } }
        }
}

/** К5: a settings-like group — label, three rows with lines between them, a one-row danger group. */
@Composable
private fun RowGroupSample() {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellSectionLabel("Общее")
        CoShellRowGroup {
            row { CoShellListRow(title = "Валюты отчётов", subtitle = "Основная: USD", onClick = {}) }
            row { CoShellSwitchRow(title = "Встряхнуть — новая операция", subtitle = "Пока открыто приложение", checked = true, onCheckedChange = {}) }
            row { CoShellListRow(title = "Обновление приложения", subtitle = "Версия 1.43 · последняя", onClick = {}) }
        }
        CoShellSectionLabel("Данные")
        CoShellRowGroup { row { CoShellListRow(title = "Стереть все данные", subtitle = "Сначала — копия", onClick = {}) } }
    }
}

/** #155: dense menu rows of «Ещё» — a coloured square, title, a grey line, a chevron. */
@Composable
private fun MenuRowsSample() {
    CoShellRowGroup(Modifier.padding(Spacing.l)) {
        row { CoShellMenuRow(R.drawable.ic_category, categoryColor(CategoryPalette.colors[9]), "Категории", {}, subtitle = "Папки, иконки и цвета") }
        row { CoShellMenuRow(R.drawable.ic_currency_exchange, categoryColor(CategoryPalette.colors[6]), "Курсы валют", {}, subtitle = "Обновляются сами раз в день") }
        row { CoShellMenuRow(R.drawable.ic_contrast, categoryColor(CategoryPalette.colors[2]), "Внешний вид", {}, subtitle = "Вольт · тёмная") }
    }
}
