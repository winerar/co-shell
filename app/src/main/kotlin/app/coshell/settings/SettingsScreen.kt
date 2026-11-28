package app.coshell.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ReminderSettings
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.newmark.NewFeatures
import app.coshell.newmark.newMarkPill
import app.coshell.newmark.newMarks
import app.coshell.recurring.notify.ReminderSettingsViewModel
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.theme.components.labelRes
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** Where a place of «Настройки» leads; the graph turns a [SettingsTarget] into a screen (`navigation/SettingsGraph.kt`). */
data class SettingsActions(
    val onBack: () -> Unit = {},
    val onOpen: (SettingsTarget) -> Unit = {},
)

/** What the index says under each group: the current theme and palette, the reminders, the totals currencies (main first). */
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ThemePalette = ThemePalette.LIME,
    val reminders: Boolean = true,
    val reminderTime: Int = ReminderSettings.DEFAULT_TIME,
    val currencies: String = "",
    /** Places wearing «новое» (`NewMarkCenter`): a group shows the pill while something inside it is new. */
    val newMarks: Set<String> = emptySet(),
)

@Composable
fun SettingsRoute(
    actions: SettingsActions,
    theme: ThemeSettingsViewModel = hiltViewModel(),
    reminders: ReminderSettingsViewModel = hiltViewModel(),
    accounts: SettingsAccountsViewModel = hiltViewModel(),
) {
    val mode by theme.mode.collectAsStateWithLifecycle()
    val palette by theme.palette.collectAsStateWithLifecycle()
    val remindersOn by reminders.enabled.collectAsStateWithLifecycle()
    val reminderTime by reminders.time.collectAsStateWithLifecycle()
    val codes by accounts.currencies.collectAsStateWithLifecycle()
    val joined = codes.fold("") { acc, c -> if (acc.isEmpty()) c else stringResource(R.string.settings_join, acc, c) }
    SettingsScreen(
        SettingsState(mode ?: ThemeMode.SYSTEM, palette ?: ThemePalette.LIME, remindersOn, reminderTime, joined, newMarks()),
        actions,
    )
}

/**
 * «Ещё → Настройки» (poll 2026-11-28 «А», boards Set1Root, Set1RootSearch): the groups as one short list with what is set now
 * under each name — «Этот телефон» and «Для всей семьи»; every setting is one tap further, on its group's screen. «Найти настройку»
 * finds a setting by its name and opens its screen ([SETTINGS_ENTRIES], [SettingsMatch]).
 */
@Composable
fun SettingsScreen(state: SettingsState, actions: SettingsActions, initialQuery: String = "") {
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    Scaffold(topBar = { ScreenTopBar(stringResource(R.string.settings_title), onBack = actions.onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = underBars(padding)) {
            item(key = "search") {
                CoShellSearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = stringResource(R.string.settings_search),
                    clearLabel = stringResource(R.string.search_clear),
                    modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s),
                )
            }
            if (query.isBlank()) index(state, actions) else results(query, actions)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.index(state: SettingsState, actions: SettingsActions) {
    item(key = "phone") {
        SectionTitle(stringResource(R.string.settings_section_phone))
        CoShellRowGroup(Modifier.padding(horizontal = Spacing.l)) {
            row {
                GroupRow(
                    DesignR.drawable.ic_contrast, R.string.appearance_title,
                    stringResource(R.string.settings_join, stringResource(state.themeMode.labelRes), stringResource(state.palette.labelRes)),
                    isNew = NewFeatures.SETTINGS_LOOK in state.newMarks,
                ) { actions.onOpen(SettingsTarget.LOOK) }
            }
            row { GroupRow(DesignR.drawable.ic_add, R.string.settings_input_title, stringResource(R.string.settings_index_input)) { actions.onOpen(SettingsTarget.INPUT) } }
            row {
                GroupRow(
                    DesignR.drawable.ic_refresh, R.string.settings_notices_title,
                    if (state.reminders) stringResource(R.string.settings_index_notices_on, clockText(state.reminderTime)) else stringResource(R.string.settings_index_notices_off),
                ) { actions.onOpen(SettingsTarget.NOTICES) }
            }
            row {
                GroupRow(
                    DesignR.drawable.ic_home, R.string.settings_screens_title, stringResource(R.string.settings_index_screens),
                    isNew = NewFeatures.SETTINGS_SCREENS in state.newMarks,
                ) { actions.onOpen(SettingsTarget.SCREENS) }
            }
            row { GroupRow(DesignR.drawable.ic_shield_lock, R.string.privacy_title, stringResource(R.string.privacy_menu_hint)) { actions.onOpen(SettingsTarget.PRIVACY) } }
            row { GroupRow(DesignR.drawable.ic_lightbulb, R.string.settings_help_title, stringResource(R.string.settings_index_help)) { actions.onOpen(SettingsTarget.HELP) } }
        }
    }
    item(key = "family") {
        SectionTitle(stringResource(R.string.settings_section_family))
        CoShellRowGroup(Modifier.padding(horizontal = Spacing.l)) {
            row {
                GroupRow(
                    DesignR.drawable.ic_payments, R.string.settings_money_title,
                    state.currencies.ifEmpty { stringResource(R.string.settings_index_money) },
                ) { actions.onOpen(SettingsTarget.MONEY) }
            }
            row { GroupRow(DesignR.drawable.ic_archive, R.string.settings_data_title, stringResource(R.string.settings_index_data)) { actions.onOpen(SettingsTarget.DATA) } }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.results(query: String, actions: SettingsActions) {
    item(key = "results") {
        val names = SETTINGS_ENTRIES.associateWith { stringResource(it.title) to stringResource(it.group) }
        val found = SettingsMatch.search(query, SETTINGS_ENTRIES, { names.getValue(it).first }, { names.getValue(it).second })
        if (found.isEmpty()) {
            CoShellHint(stringResource(R.string.settings_search_none), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m))
        } else {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s)) {
                found.forEach { entry ->
                    row {
                        CoShellListRow(
                            title = names.getValue(entry).first,
                            subtitle = names.getValue(entry).second,
                            onClick = { actions.onOpen(entry.target) },
                            dense = true,
                        )
                    }
                }
            }
        }
    }
}

/** A group of the index: its icon, name, and what is set now (or what is inside); «новое» while something inside is new. */
@Composable
private fun GroupRow(@DrawableRes icon: Int, @StringRes title: Int, value: String, isNew: Boolean = false, onClick: () -> Unit) =
    CoShellMenuRow(icon, stringResource(title), onClick, subtitle = value, mark = newMarkPill(isNew))
