package app.coshell.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.coshell.family.sync.SyncScheduler
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.importer.wallet.components.ImportColors
import app.coshell.core.domain.settings.ReminderSettings
import app.coshell.recurring.notify.RecurringPaymentsScheduler
import app.coshell.recurring.notify.ReminderSettingsViewModel
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

// The group screens of «Настройки» (boards Set1Entry, Set1Notices, Set1Money, Set1Data). Only what exists today:
// the rest of each board (partner spending, week start…) lands one setting at a time.

/** «Ввод операции»: the shake gesture. */
@Composable
fun InputSettingsRoute(
    onBack: () -> Unit,
    viewModel: ShakeSettingsViewModel = hiltViewModel(),
    screens: ScreenOptionsViewModel = hiltViewModel(),
) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val options by screens.options.collectAsStateWithLifecycle()
    InputSettingsScreen(enabled, viewModel::setEnabled, options.haptics, { v -> screens.change { it.copy(haptics = v) } }, onBack)
}

@Composable
fun InputSettingsScreen(
    shakeEnabled: Boolean,
    onShakeChange: (Boolean) -> Unit,
    hapticsEnabled: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    GroupScaffold(R.string.settings_input_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_input_quick)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        leading = { MenuIcon(DesignR.drawable.ic_mobile_vibrate, categoryColor(CategoryPalette.colors[8])) },
                        title = stringResource(R.string.shake_to_add_title),
                        subtitle = stringResource(R.string.shake_to_add_hint),
                        checked = shakeEnabled,
                        onCheckedChange = onShakeChange,
                    )
                }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_input_feedback)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_input_haptics),
                        subtitle = stringResource(R.string.settings_input_haptics_hint),
                        checked = hapticsEnabled,
                        onCheckedChange = onHapticsChange,
                    )
                }
            }
        }
    }
}

/** «Уведомления»: payment reminders and their time. */
@Composable
fun NoticeSettingsRoute(onBack: () -> Unit, viewModel: ReminderSettingsViewModel = hiltViewModel()) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val hour by viewModel.hour.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext
    NoticeSettingsScreen(
        remindersEnabled = enabled,
        onRemindersChange = viewModel::setEnabled,
        hour = hour,
        onHourChange = { h -> viewModel.setHour(h, after = { RecurringPaymentsScheduler.schedule(context, replace = true) }) },
        onBack = onBack,
    )
}

@Composable
fun NoticeSettingsScreen(remindersEnabled: Boolean, onRemindersChange: (Boolean) -> Unit, hour: Int, onHourChange: (Int) -> Unit, onBack: () -> Unit) {
    GroupScaffold(R.string.settings_notices_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_notices_payments)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        leading = { MenuIcon(DesignR.drawable.ic_refresh, categoryColor(CategoryPalette.colors[14])) },
                        title = stringResource(R.string.reminders_title),
                        subtitle = stringResource(R.string.reminders_hint),
                        checked = remindersEnabled,
                        onCheckedChange = onRemindersChange,
                    )
                }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_notices_when)) }
        item {
            PickCard(
                title = stringResource(R.string.settings_notices_time),
                labels = ReminderSettings.HOURS.map { stringResource(R.string.settings_notices_hour, it) },
                selected = ReminderSettings.HOURS.indexOf(hour).coerceAtLeast(0),
                onSelect = { onHourChange(ReminderSettings.HOURS[it]) },
            )
        }
    }
}

/** «Деньги и даты»: what is shared by the family. */
@Composable
fun MoneySettingsScreen(onCurrencies: () -> Unit, onBack: () -> Unit) {
    GroupScaffold(R.string.settings_money_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_money_family)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellMenuRow(
                        DesignR.drawable.ic_payments, categoryColor(CategoryPalette.colors[17]),
                        stringResource(R.string.report_currencies_title), onCurrencies,
                    )
                }
            }
        }
    }
}

/** «Данные и копия»: backup, import, the network the sync may use, wiping. */
@Composable
fun DataSettingsRoute(
    onBackup: () -> Unit,
    onImport: () -> Unit,
    onWipe: () -> Unit,
    onBack: () -> Unit,
    viewModel: ScreenOptionsViewModel = hiltViewModel(),
) {
    val options by viewModel.options.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext
    DataSettingsScreen(
        wifiOnly = options.syncWifiOnly,
        onWifiOnlyChange = { v -> viewModel.change(after = { SyncScheduler.schedulePeriodic(context) }) { it.copy(syncWifiOnly = v) } },
        onBackup = onBackup, onImport = onImport, onWipe = onWipe, onBack = onBack,
    )
}

@Composable
fun DataSettingsScreen(
    wifiOnly: Boolean,
    onWifiOnlyChange: (Boolean) -> Unit,
    onBackup: () -> Unit,
    onImport: () -> Unit,
    onWipe: () -> Unit,
    onBack: () -> Unit,
) {
    val c = CategoryPalette.colors
    GroupScaffold(R.string.settings_data_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_data_section)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row { CoShellMenuRow(DesignR.drawable.ic_settings_backup_restore, categoryColor(c[5]), stringResource(R.string.backup_title), onBackup) }
                row { CoShellMenuRow(DesignR.drawable.ic_download, categoryColor(c[ImportColors.MENU]), stringResource(R.string.import_title), onImport) }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_data_network)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_data_wifi), subtitle = stringResource(R.string.settings_data_wifi_hint),
                        checked = wifiOnly, onCheckedChange = onWifiOnlyChange,
                    )
                }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_data_careful)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row { CoShellMenuRow(DesignR.drawable.ic_delete, categoryColor(c[0]), stringResource(R.string.wipe_title), onWipe) }
            }
        }
    }
}

@Composable
internal fun GroupScaffold(title: Int, onBack: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    Scaffold(topBar = { ScreenTopBar(stringResource(title), onBack = onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), content = content)
    }
}

@Preview(showBackground = true)
@Composable
private fun DataSettingsPreview() {
    CoShellTheme { DataSettingsScreen(false, {}, {}, {}, {}, {}) }
}
