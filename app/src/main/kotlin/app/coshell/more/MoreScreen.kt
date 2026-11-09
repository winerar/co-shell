package app.coshell.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.motion.staggeredEntrance
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.more.components.MoreTile
import app.coshell.ui.list.ActionRow
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import app.coshell.core.designsystem.R as DesignR

// Menu tab: sections of the app (categories, rates, family, backup, feedback) and «Настройки».
// Device preferences live on the Settings screen (issue #30).

data class MoreActions(
    val onCategories: () -> Unit = {},
    val onRates: () -> Unit = {},
    val onFamily: () -> Unit = {},
    val onBackup: () -> Unit = {},
    val onFeedback: () -> Unit = {},
    val onSettings: () -> Unit = {},
)

@Composable
fun MoreScreen(actions: MoreActions) {
    val scroll = rememberTabScrollBehavior()
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.nav_more), scroll) },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        val tiles = listOf(
            Tile(DesignR.drawable.ic_category, R.string.categories_title, R.string.categories_menu_hint, CategoryPalette.colors[9], actions.onCategories),
            Tile(DesignR.drawable.ic_currency_exchange, R.string.rates_title, R.string.rates_menu_hint, CategoryPalette.colors[6], actions.onRates),
            Tile(DesignR.drawable.ic_group, R.string.family_title, R.string.family_menu_hint, CategoryPalette.colors[3], actions.onFamily),
            Tile(DesignR.drawable.ic_settings_backup_restore, R.string.backup_title, R.string.backup_menu_hint, CategoryPalette.colors[11], actions.onBackup),
            Tile(DesignR.drawable.ic_feedback, R.string.feedback_title, R.string.feedback_menu_hint, CategoryPalette.colors[18], actions.onFeedback),
        )
        // Bento grid of sections (design-refactor Step 5), «Настройки» as a row under it.
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, top = Spacing.s, bottom = Sizes.fabClearance),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            itemsIndexed(tiles, key = { _, tile -> tile.title }) { index, tile ->
                MoreTile(tile.icon, stringResource(tile.title), stringResource(tile.subtitle), tile.color, tile.onClick, Modifier.staggeredEntrance(index))
            }
            item(key = "settings", span = { GridItemSpan(maxLineSpan) }) {
                ActionRow(
                    icon = DesignR.drawable.ic_settings,
                    title = stringResource(R.string.settings_title),
                    subtitle = stringResource(R.string.settings_menu_hint),
                    onClick = actions.onSettings,
                    opensScreen = true,
                )
            }
        }
    }
}

private data class Tile(val icon: Int, val title: Int, val subtitle: Int, val color: Long, val onClick: () -> Unit)

@Preview(showBackground = true)
@Composable
private fun MoreScreenPreview() {
    CoShellTheme { MoreScreen(MoreActions()) }
}
