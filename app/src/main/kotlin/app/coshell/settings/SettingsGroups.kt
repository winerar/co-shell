package app.coshell.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import app.coshell.recurring.notify.ReminderSettingsViewModel
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

// The group screens of «Настройки» (boards Set1Entry, Set1Notices, Set1Money, Set1Data). Only what exists today:
// the rest of each board (vibration, partner spending, week start, Wi-Fi sync…) lands one setting at a time.

/** «Ввод операции»: the shake gesture. */
@Composable
fun InputSettingsRoute(onBack: () -> Unit, viewModel: ShakeSettingsViewModel = hiltViewModel()) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    InputSettingsScreen(enabled, viewModel::setEnabled, onBack)
}

@Composable
fun InputSettingsScreen(shakeEnabled: Boolean, onShakeChange: (Boolean) -> Unit, onBack: () -> Unit) {
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
    }
}

/** «Уведомления»: payment reminders. */
@Composable
fun NoticeSettingsRoute(onBack: () -> Unit, viewModel: ReminderSettingsViewModel = hiltViewModel()) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    NoticeSettingsScreen(enabled, viewModel::setEnabled, onBack)
}

@Composable
fun NoticeSettingsScreen(remindersEnabled: Boolean, onRemindersChange: (Boolean) -> Unit, onBack: () -> Unit) {
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

/** «Данные и копия»: backup, import, wiping. */
@Composable
fun DataSettingsScreen(onBackup: () -> Unit, onImport: () -> Unit, onWipe: () -> Unit, onBack: () -> Unit) {
    val c = CategoryPalette.colors
    GroupScaffold(R.string.settings_data_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_data_section)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row { CoShellMenuRow(DesignR.drawable.ic_settings_backup_restore, categoryColor(c[5]), stringResource(R.string.backup_title), onBackup) }
                row { CoShellMenuRow(DesignR.drawable.ic_download, categoryColor(c[ImportColors.MENU]), stringResource(R.string.import_title), onImport) }
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
    CoShellTheme { DataSettingsScreen({}, {}, {}, {}) }
}
