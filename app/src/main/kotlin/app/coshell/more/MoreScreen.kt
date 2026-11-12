package app.coshell.more

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.R
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.motion.staggeredEntrance
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.more.components.VersionCard
import app.coshell.more.components.moreSections
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll

// Menu tab (board 154А, #155): the version card on top, then the rarely used sections as rows in three groups
// (Деньги · Семья и данные · Приложение). The former «Настройки» screen is gone: its items live in the groups.

/** What the rows say about their sections; defaults make previews short. */
data class MoreState(
    val versionName: String = "",
    val build: Int = 0,
    /** Version name of a newer release («0.38»), or null when up to date (unknown counts as up to date). */
    val availableVersion: String? = null,
    val mainCurrency: String? = null,
    /** «Вольт · тёмная» under «Внешний вид». */
    val appearance: String? = null,
)

data class MoreActions(
    val onCategories: () -> Unit = {},
    val onBudgets: () -> Unit = {},
    val onRates: () -> Unit = {},
    val onReportCurrencies: () -> Unit = {},
    val onFamily: () -> Unit = {},
    val onBackup: () -> Unit = {},
    val onImport: () -> Unit = {},
    val onAppearance: () -> Unit = {},
    val onFeedback: () -> Unit = {},
    /** Tap on the version card: the update screen. */
    val onOpenUpdate: () -> Unit = {},
    /** «Обновить» on the card: the download starts right away and the update screen opens. */
    val onStartUpdate: () -> Unit = {},
)

@Composable
fun MoreScreen(actions: MoreActions, state: MoreState = MoreState()) {
    val scroll = rememberTabScrollBehavior()
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.nav_more), scroll) },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        val sections = moreSections(state, actions)
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(top = Spacing.s, bottom = Sizes.fabClearance),
        ) {
            item(key = "version") {
                VersionCard(
                    versionName = state.versionName,
                    build = state.build,
                    availableVersion = state.availableVersion,
                    onOpen = actions.onOpenUpdate,
                    onUpdate = actions.onStartUpdate,
                    modifier = Modifier.padding(horizontal = Spacing.l).staggeredEntrance(0),
                )
            }
            itemsIndexed(sections, key = { _, section -> section.title }) { index, section ->
                SectionTitle(stringResource(section.title))
                CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).staggeredEntrance(index + 1)) {
                    section.items.forEach { item ->
                        row {
                            CoShellMenuRow(
                                icon = item.icon,
                                color = categoryColor(item.color),
                                title = stringResource(item.title),
                                subtitle = item.subtitle,
                                onClick = item.onClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MoreScreenPreview() {
    CoShellTheme { MoreScreen(MoreActions(), MoreState("0.37", 400, mainCurrency = "USD", appearance = "Вольт · системная")) }
}

@Preview(showBackground = true)
@Composable
private fun MoreScreenUpdatePreview() {
    CoShellTheme(darkTheme = true) { MoreScreen(MoreActions(), MoreState("0.37", 400, availableVersion = "0.38", mainCurrency = "ARS")) }
}
