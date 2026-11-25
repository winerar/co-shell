package app.coshell.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.coshell.family.sync.SyncScheduler
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.components.underBars
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** «Данные и копия»: backup, import, the network the sync may use, wiping. */
@Composable
fun DataSettingsRoute(
    onBackup: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onWipe: () -> Unit,
    onBack: () -> Unit,
    viewModel: ScreenOptionsViewModel = hiltViewModel(),
) {
    val options by viewModel.options.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext
    DataSettingsScreen(
        wifiOnly = options.syncWifiOnly,
        onWifiOnlyChange = { v -> viewModel.change(after = { SyncScheduler.schedulePeriodic(context) }) { it.copy(syncWifiOnly = v) } },
        onBackup = onBackup, onImport = onImport, onExport = onExport, onWipe = onWipe, onBack = onBack,
    )
}

@Composable
fun DataSettingsScreen(
    wifiOnly: Boolean,
    onWifiOnlyChange: (Boolean) -> Unit,
    onBackup: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onWipe: () -> Unit,
    onBack: () -> Unit,
) {
    val c = CategoryPalette.colors
    GroupScaffold(R.string.settings_data_title, onBack) {
        item { SectionTitle(stringResource(R.string.settings_data_section)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row { CoShellMenuRow(DesignR.drawable.ic_settings_backup_restore, stringResource(R.string.backup_title), onBackup) }
                row { CoShellMenuRow(DesignR.drawable.ic_upload, stringResource(R.string.export_title), onExport, subtitle = stringResource(R.string.export_menu_hint)) }
                row { CoShellMenuRow(DesignR.drawable.ic_download, stringResource(R.string.import_title), onImport) }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_data_network)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row {
                    CoShellSwitchRow(
                        title = stringResource(R.string.settings_data_wifi),
                        checked = wifiOnly, onCheckedChange = onWifiOnlyChange,
                    )
                }
            }
        }
        item { SectionTitle(stringResource(R.string.settings_data_careful)) }
        item {
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                row { CoShellMenuRow(DesignR.drawable.ic_delete, stringResource(R.string.wipe_title), onWipe) }
            }
        }
    }
}

@Composable
internal fun GroupScaffold(title: Int, onBack: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    Scaffold(topBar = { ScreenTopBar(stringResource(title), onBack = onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = underBars(padding), content = content)
    }
}

@Preview(showBackground = true)
@Composable
private fun DataSettingsPreview() {
    CoShellTheme { DataSettingsScreen(false, {}, {}, {}, {}, {}, {}) }
}
