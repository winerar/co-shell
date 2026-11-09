package app.coshell.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.currencies.list.ReportCurrenciesViewModel
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.theme.components.ThemeModeRow
import app.coshell.ui.list.ActionRow
import app.coshell.ui.list.SwitchRow
import app.coshell.core.designsystem.R as DesignR

/** «Ещё → Настройки» (issue #30): device preferences — theme, report currencies, shake, app update. */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onReportCurrencies: () -> Unit,
    onUpdate: () -> Unit,
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
    reportCurrencies: ReportCurrenciesViewModel = hiltViewModel(),
    themeSettings: ThemeSettingsViewModel = hiltViewModel(),
) {
    val shakeEnabled by shakeSettings.enabled.collectAsStateWithLifecycle()
    val currencies by reportCurrencies.state.collectAsStateWithLifecycle()
    val themeMode by themeSettings.mode.collectAsStateWithLifecycle()
    SettingsScreen(
        state = SettingsState(
            themeMode = themeMode ?: ThemeMode.SYSTEM,
            mainCurrency = currencies.currencies.firstOrNull(),
            shakeEnabled = shakeEnabled,
        ),
        actions = SettingsActions(
            onBack = onBack,
            onThemeModeChange = themeSettings::setMode,
            onReportCurrencies = onReportCurrencies,
            onShakeEnabledChange = shakeSettings::setEnabled,
            onUpdate = onUpdate,
        ),
    )
}

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val mainCurrency: String? = null,
    val shakeEnabled: Boolean = true,
)

data class SettingsActions(
    val onBack: () -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onReportCurrencies: () -> Unit = {},
    val onShakeEnabledChange: (Boolean) -> Unit = {},
    val onUpdate: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: SettingsState, actions: SettingsActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    CoShellIconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { ThemeModeRow(selected = state.themeMode, onSelect = actions.onThemeModeChange) }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_payments,
                    title = stringResource(R.string.report_currencies_title),
                    subtitle = state.mainCurrency?.let { stringResource(R.string.report_currencies_menu_hint, it) },
                    onClick = actions.onReportCurrencies,
                    opensScreen = true,
                )
            }
            item {
                SwitchRow(
                    icon = DesignR.drawable.ic_mobile_vibrate,
                    title = stringResource(R.string.shake_to_add_title),
                    subtitle = stringResource(R.string.shake_to_add_hint),
                    checked = state.shakeEnabled,
                    onCheckedChange = actions.onShakeEnabledChange,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_deployed_code_update,
                    title = stringResource(R.string.update_title),
                    onClick = actions.onUpdate,
                    opensScreen = true,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CoShellTheme { SettingsScreen(SettingsState(mainCurrency = "USD"), SettingsActions()) }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenDarkPreview() {
    CoShellTheme(darkTheme = true) { SettingsScreen(SettingsState(mainCurrency = "ARS"), SettingsActions()) }
}
