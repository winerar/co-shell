package app.coshell.more

import app.coshell.hints.HintSlot
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
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellMenuGrid
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.MenuTile
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.motion.staggeredEntrance
import app.coshell.core.designsystem.theme.CoShellTheme

import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.more.components.moreAppRows
import app.coshell.more.components.moreGrids
import app.coshell.newmark.NewFeatures
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll

// Menu tab (poll 2026-11-28 «А», boards More, MoreTiles): the sections as two grids of icons (Деньги · Валюты и семья) and three
// rows of «Приложение» — the whole menu on one screen. Backup, import and export are one place «Данные» (= «Настройки → Данные»);
// «Конфиденциальность» lives in «Настройки». Sections — `components/MoreSections.kt`.

/** What the rows say about their sections; defaults make previews short. */
data class MoreState(
    val versionName: String = "",
    val build: Int = 0,
    /** Version name of a newer release («0.38»), or null when up to date (unknown counts as up to date). */
    val availableVersion: String? = null,
    /** Keys of the rows that wear the «новое» pill (#335, `NewFeatures`). */
    val newMarks: Set<String> = emptySet(),
    /** Drafts waiting (#586): the count on the «Черновики» row. */
    val draftCount: Int = 0,
)

data class MoreActions(
    val onCategories: () -> Unit = {},
    val onAccounts: () -> Unit = {},
    val onBudgets: () -> Unit = {},
    /** «Регулярные платежи»: always reachable here, also when «Скоро» on «Обзор» is hidden (#403). */
    val onRecurring: () -> Unit = {},
    val onRates: () -> Unit = {},
    val onConverter: () -> Unit = {},
    val onFamily: () -> Unit = {},
    /** «Данные»: backup, import, export, wiping (`DataSettingsScreen`). */
    val onData: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val onFeedback: () -> Unit = {},
    /** «Быстрые заметки» and «Черновики» (#586). */
    val onNotes: () -> Unit = {},
    val onDrafts: () -> Unit = {},
    /** «Долги» (#552). */
    val onDebts: () -> Unit = {},
    /** «Цели» (#768). */
    val onGoals: () -> Unit = {},
    /** «О приложении»: version, what's new and the update. */
    val onOpenUpdate: () -> Unit = {},
    /** A row with the «новое» pill was opened: the pill goes away. */
    val onOpenedNew: (String) -> Unit = {},
)

@Composable
fun MoreScreen(actions: MoreActions, state: MoreState = MoreState()) {
    val scroll = rememberTabScrollBehavior()
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.nav_more), scroll) },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        val grids = moreGrids(actions, state.draftCount)
        val app = moreAppRows(actions, state.versionName, state.build, state.availableVersion)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = underBars(padding, PaddingValues(top = Spacing.s, bottom = Spacing.l)),
        ) {
            item(key = "hint") { HintSlot(Modifier.padding(horizontal = Spacing.l)) }
            itemsIndexed(grids, key = { _, grid -> grid.title }) { index, grid ->
                SectionTitle(stringResource(grid.title))
                CoShellMenuGrid(
                    grid.items.map { item ->
                        val isNew = item.newKey != null && item.newKey in state.newMarks
                        MenuTile(
                            icon = item.icon,
                            label = stringResource(item.title),
                            onClick = {
                                if (isNew) actions.onOpenedNew(item.newKey!!)
                                item.onClick()
                            },
                            mark = when {
                                isNew -> stringResource(R.string.new_mark)
                                else -> item.count?.toString()
                            },
                        )
                    },
                    Modifier.padding(horizontal = Spacing.l).staggeredEntrance(index),
                )
            }
            item(key = "app") {
                SectionTitle(stringResource(R.string.more_section_app))
                CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).staggeredEntrance(grids.size)) {
                    app.forEach { item ->
                        val isNew = item.newKey != null && item.newKey in state.newMarks
                        row {
                            CoShellMenuRow(
                                icon = item.icon,
                                title = stringResource(item.title),
                                subtitle = item.subtitle,
                                onClick = {
                                    if (isNew) actions.onOpenedNew(item.newKey!!)
                                    item.onClick()
                                },
                                mark = if (isNew || item.fresh) {
                                    { CoShellStatusChip(stringResource(R.string.new_mark), StatusTone.Fresh) }
                                } else {
                                    null
                                },
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
    CoShellTheme { MoreScreen(MoreActions(), MoreState("0.37", 400, newMarks = setOf(NewFeatures.RECURRING))) }
}

@Preview(showBackground = true)
@Composable
private fun MoreScreenUpdatePreview() {
    CoShellTheme(darkTheme = true) { MoreScreen(MoreActions(), MoreState("0.37", 400, availableVersion = "0.38")) }
}
