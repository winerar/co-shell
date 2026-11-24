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
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.motion.staggeredEntrance
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.more.components.QuietVersion
import app.coshell.more.components.VersionCard
import app.coshell.more.components.moreSections
import app.coshell.newmark.NewFeatures
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll

// Menu tab (poll 2026-11-20, Б; boards MoreB, MoreBEnd, MoreBUpdate): rarely used sections as rows in four groups
// (Деньги · Валюты · Семья и данные · Приложение); the version card goes on top only when an update is out, otherwise a quiet
// line at the bottom. The former «Настройки» screen is gone: its items live in the groups.

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
    val onBackup: () -> Unit = {},
    val onImport: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onSettings: () -> Unit = {},
    /** «Конфиденциальность» (#145). */
    val onPrivacy: () -> Unit = {},
    val onFeedback: () -> Unit = {},
    /** «Быстрые заметки» and «Черновики» (#586). */
    val onNotes: () -> Unit = {},
    val onDrafts: () -> Unit = {},
    /** Tap on the version card: the update screen. */
    val onOpenUpdate: () -> Unit = {},
    /** «Обновить» on the card: the download starts right away and the update screen opens. */
    val onStartUpdate: () -> Unit = {},
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
        val sections = moreSections(actions, state.draftCount)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = underBars(padding, PaddingValues(top = Spacing.s, bottom = Sizes.fabClearance)),
        ) {
            if (state.availableVersion != null) {
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
            }
            itemsIndexed(sections, key = { _, section -> section.title }) { index, section ->
                SectionTitle(stringResource(section.title))
                CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).staggeredEntrance(index + 1)) {
                    section.items.forEach { item ->
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
                                mark = when {
                                    isNew -> {
                                        { CoShellStatusChip(stringResource(R.string.new_mark), StatusTone.Fresh) }
                                    }
                                    item.count != null -> {
                                        { CoShellStatusChip(item.count.toString(), StatusTone.Fresh) }
                                    }
                                    else -> null
                                },
                            )
                        }
                    }
                }
            }
            if (state.availableVersion == null) {
                item(key = "version") { QuietVersion(state.versionName, state.build, actions.onOpenUpdate) }
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
