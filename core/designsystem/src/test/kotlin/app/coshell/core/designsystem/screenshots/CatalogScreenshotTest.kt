package app.coshell.core.designsystem.screenshots

import app.coshell.core.designsystem.components.CoShellMenuRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.catalog.ChartsCatalog
import app.coshell.core.designsystem.catalog.ForecastCatalog
import app.coshell.core.designsystem.catalog.HeatCatalog
import app.coshell.core.designsystem.catalog.CheckMarkSample
import app.coshell.core.designsystem.catalog.PinSample
import app.coshell.core.designsystem.catalog.ComponentsCatalog
import app.coshell.core.designsystem.catalog.ConfirmStackCatalog
import app.coshell.core.designsystem.catalog.ModalsCatalog
import app.coshell.core.designsystem.catalog.CounterSample
import app.coshell.core.designsystem.catalog.DetailsSample
import app.coshell.core.designsystem.catalog.SegmentedSample
import app.coshell.core.designsystem.catalog.TokensCatalog
import app.coshell.core.designsystem.components.CoShellBottomBar
import app.coshell.core.designsystem.components.CoShellPaletteSwatch
import app.coshell.core.designsystem.components.CoShellTab
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.Column
import app.coshell.core.designsystem.components.CoShellMenuGrid
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.MenuTile
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

    /** #168: bars with the past period, columns by month, the ring and bullets. */
    @Test
    fun `charts dark`() = shot("charts_dark", dark = true) { ChartsCatalog() }

    @Test
    fun `charts light`() = shot("charts_light") { ChartsCatalog() }

    /** #192, #195, #770: the share bar, the sparkline and the balance line ahead. */
    @Test
    fun `forecast dark`() = shot("forecast_dark", dark = true) { ForecastCatalog() }

    @Test
    fun `forecast light`() = shot("forecast_light") { ForecastCatalog() }

    /** #553: the spending calendar. */
    @Test
    fun `heat dark`() = shot("heat_dark", dark = true) { HeatCatalog() }

    @Test
    fun `heat light`() = shot("heat_light") { HeatCatalog() }

    @Test
    fun `tokens light`() = shot("tokens_light") { TokensCatalog() }

    @Test
    fun `tokens dark`() = shot("tokens_dark", dark = true) { TokensCatalog() }

    /** The app's tabs (#140): Обзор · Операции · [+] · Отчёты · Ещё. */
    private val barTabs = listOf(
        CoShellTab(R.drawable.ic_home, "Обзор", selected = true),
        CoShellTab(R.drawable.ic_receipt_long, "Операции", selected = false),
        CoShellTab(R.drawable.ic_bar_chart, "Отчёты", selected = false),
        CoShellTab(R.drawable.ic_more_horiz, "Ещё", selected = false),
    )

    @Test
    fun `bottom bar dark`() = shot("bottom_bar_dark", dark = true) { BottomBarSample() }

    /** The light theme has its own selection look (neon with an edge); no dash under the tab (#511). */
    @Test
    fun `bottom bar light`() = shot("bottom_bar_light") { BottomBarSample() }

    @Composable
    private fun BottomBarSample() = CoShellBottomBar(tabs = barTabs, onTab = {}, addLabel = "Новая операция", onAdd = {})

    /** #334, board `Fb334A`: entry closed until the update — the «+» is muted, with a lock. */
    @Test
    fun `bottom bar locked dark`() = shot("bottom_bar_locked_dark", dark = true) {
        CoShellBottomBar(
            tabs = barTabs,
            onTab = {},
            addLabel = "Добавить — после обновления",
            onAdd = {},
            addLocked = true,
        )
    }

    @Test
    fun `segmented dark`() = shot("segmented_dark", dark = true) { Column(Modifier.padding(Spacing.l)) { SegmentedSample() } }

    @Test
    fun `segmented light`() = shot("segmented_light") { Column(Modifier.padding(Spacing.l)) { SegmentedSample() } }

    @Test
    fun `counter dark`() = shot("counter_dark", dark = true) { Column(Modifier.padding(Spacing.l)) { CounterSample() } }

    @Test
    fun `counter light`() = shot("counter_light") { Column(Modifier.padding(Spacing.l)) { CounterSample() } }

    @Test
    fun `detail chips dark`() = shot("detail_chips_dark", dark = true) { Column(Modifier.padding(Spacing.l)) { DetailsSample() } }

    @Test
    fun `detail chips light`() = shot("detail_chips_light") { Column(Modifier.padding(Spacing.l)) { DetailsSample() } }

    @Test
    fun `row group dark`() = shot("row_group_dark", dark = true) { RowGroupSample() }

    @Test
    fun `row group light`() = shot("row_group_light") { RowGroupSample() }

    @Test
    fun `menu grid dark`() = shot("menu_grid_dark", dark = true) { Column(Modifier.padding(Spacing.l)) { MenuGridSample() } }

    @Test
    fun `menu grid light`() = shot("menu_grid_light") { Column(Modifier.padding(Spacing.l)) { MenuGridSample() } }

    /** #345: the question card and the sheet with the day grid. */
    @Test
    fun `modals dark`() = shot("modals_dark", dark = true) { ModalsCatalog() }

    @Test
    fun `modals light`() = shot("modals_light") { ModalsCatalog() }

    @Test
    fun `confirm stack dark`() = shot("confirm_stack_dark", dark = true) { ConfirmStackCatalog() }

    @Test
    fun `confirm stack light`() = shot("confirm_stack_light") { ConfirmStackCatalog() }

    @Test
    fun `check marks dark`() = shot("check_marks_dark", dark = true) { CheckMarkSample() }

    @Test
    fun `check marks light`() = shot("check_marks_light") { CheckMarkSample() }

    @Test
    fun `pin pad dark`() = shot("pin_pad_dark", dark = true) { PinSample() }

    @Test
    fun `pin pad light`() = shot("pin_pad_light") { PinSample() }

    @Test
    fun `menu rows dark`() = shot("menu_rows_dark", dark = true) { MenuRowsSample() }

    @Test
    fun `menu rows light`() = shot("menu_rows_light") { MenuRowsSample() }

    @Test
    fun `palettes light`() = shot("palettes_light") {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            CoShellPalette.entries.chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                    row.forEach { palette ->
                        CoShellPaletteSwatch(palette, palette.name, selected = palette == CoShellPalette.Lime, onClick = {})
                    }
                }
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
private fun MenuGridSample() {
    CoShellMenuGrid(
        listOf(
            MenuTile(R.drawable.ic_account_balance_wallet, "Счета", {}),
            MenuTile(R.drawable.ic_category, "Категории", {}),
            MenuTile(R.drawable.ic_savings, "Бюджеты", {}),
            MenuTile(R.drawable.ic_flag, "Цели", {}, mark = "новое"),
            MenuTile(R.drawable.ic_refresh, "Регулярные", {}),
            MenuTile(R.drawable.ic_draft, "Черновики", {}, mark = "3"),
        ),
    )
}

@Composable
private fun RowGroupSample() {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellSectionLabel("Общее")
        CoShellRowGroup {
            row { CoShellListRow(title = "Валюты итогов", subtitle = "Основная: USD", onClick = {}) }
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
        row { CoShellMenuRow(R.drawable.ic_category, "Категории", {}, subtitle = "Папки, иконки и цвета") }
        row { CoShellMenuRow(R.drawable.ic_currency_exchange, "Курсы валют", {}, subtitle = "Обновляются сами раз в день") }
        row { CoShellMenuRow(R.drawable.ic_contrast, "Внешний вид", {}, subtitle = "Вольт · тёмная") }
    }
}
