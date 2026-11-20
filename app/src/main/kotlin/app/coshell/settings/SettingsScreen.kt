package app.coshell.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.coshell.hints.HintsViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.motion.LocalThemeReveal
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ReminderSettings
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.core.domain.settings.WeekStart
import app.coshell.family.sync.SyncScheduler
import app.coshell.recurring.notify.ReminderSettingsViewModel
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.ui.top.ScreenTopBar

/** Where «Настройки» leads and what it changes in place (group cards, poll 2026-11-20, В). */
data class SettingsActions(
    val onBack: () -> Unit = {},
    val onLook: () -> Unit = {},
    val onScreens: () -> Unit = {},
    val onMoney: () -> Unit = {},
    val onInput: () -> Unit = {},
    val onNotices: () -> Unit = {},
    val onData: () -> Unit = {},
    val onCurrencies: () -> Unit = {},
    val onBackup: () -> Unit = {},
    val onThemeMode: (ThemeMode) -> Unit = {},
    val onShake: (Boolean) -> Unit = {},
    val onHaptics: (Boolean) -> Unit = {},
    val onReminders: (Boolean) -> Unit = {},
    val onWifiOnly: (Boolean) -> Unit = {},
    /** «Подсказки» on/off and «Показать подсказки заново» (moved here from «Ещё», poll 2026-11-20, Б). */
    val onHints: (Boolean) -> Unit = {},
    val onHintsReset: () -> Unit = {},
)

/** The main settings of each group, shown right on its card; [currencies] — totals currencies, main first. */
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ThemePalette = ThemePalette.LIME,
    val shake: Boolean = false,
    val haptics: Boolean = true,
    val reminders: Boolean = true,
    val reminderHour: Int = ReminderSettings.DEFAULT_HOUR,
    val currencies: String = "",
    val weekStart: WeekStart = WeekStart.SYSTEM,
    val wifiOnly: Boolean = false,
    val hints: Boolean = true,
)

@Composable
fun SettingsRoute(
    actions: SettingsActions,
    theme: ThemeSettingsViewModel = hiltViewModel(),
    shake: ShakeSettingsViewModel = hiltViewModel(),
    reminders: ReminderSettingsViewModel = hiltViewModel(),
    screens: ScreenOptionsViewModel = hiltViewModel(),
    accounts: SettingsAccountsViewModel = hiltViewModel(),
    hints: HintsViewModel = hiltViewModel(),
) {
    val hintsOn by hints.enabled.collectAsStateWithLifecycle()
    val mode by theme.mode.collectAsStateWithLifecycle()
    val palette by theme.palette.collectAsStateWithLifecycle()
    val shakeOn by shake.enabled.collectAsStateWithLifecycle()
    val remindersOn by reminders.enabled.collectAsStateWithLifecycle()
    val hour by reminders.hour.collectAsStateWithLifecycle()
    val options by screens.options.collectAsStateWithLifecycle()
    val codes by accounts.currencies.collectAsStateWithLifecycle()
    val joined = codes.fold("") { acc, c -> if (acc.isEmpty()) c else stringResource(R.string.settings_join, acc, c) }
    val reveal = LocalThemeReveal.current
    val context = LocalContext.current.applicationContext
    SettingsScreen(
        SettingsState(
            mode ?: ThemeMode.SYSTEM, palette ?: ThemePalette.LIME, shakeOn, options.haptics, remindersOn, hour, joined,
            options.weekStart, options.syncWifiOnly, hintsOn,
        ),
        actions.copy(
            onThemeMode = { m -> if (m != mode) reveal.change { theme.setMode(m) } },
            onShake = shake::setEnabled,
            onHaptics = { v -> screens.change { it.copy(haptics = v) } },
            onReminders = reminders::setEnabled,
            onWifiOnly = { v -> screens.change(after = { SyncScheduler.schedulePeriodic(context) }) { it.copy(syncWifiOnly = v) } },
            onHints = hints::setEnabled,
            onHintsReset = hints::reset,
        ),
    )
}

/**
 * «Ещё → Настройки» (boards SetCardsC, SetCardsC2): a card per group with its one or two main settings inside and «Все ›»
 * to the group screen — what is changed often takes one tap. The cards themselves — `SettingsCards.kt`.
 */
@Composable
fun SettingsScreen(state: SettingsState, actions: SettingsActions) {
    Scaffold(topBar = { ScreenTopBar(stringResource(R.string.settings_title), onBack = actions.onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = underBars(padding)) {
            item { CoShellHint(stringResource(R.string.settings_intro), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
            item(key = "look") { LookCard(state, actions) }
            item(key = "input") { InputCard(state, actions) }
            item(key = "notices") { NoticesCard(state, actions) }
            item(key = "money") { MoneyCard(state, actions) }
            item(key = "screens") { ScreensCard(actions) }
            item(key = "data") { DataCard(state, actions) }
            item(key = "help") { HelpCard(state, actions) }
        }
    }
}
