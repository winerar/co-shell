package app.coshell.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.motion.LocalThemeReveal
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.currencies.list.ReportCurrenciesViewModel
import app.coshell.ui.list.SectionTitle
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.theme.components.PaletteRow
import app.coshell.theme.components.ThemeModeRow
import app.coshell.ui.list.ActionRow
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.R as DesignR

/** «Ещё → Настройки» (issue #30): device preferences — theme and palette, report currencies, shake, app update. */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onReportCurrencies: () -> Unit,
    onUpdate: () -> Unit,
    onWipeData: () -> Unit = {},
    availableUpdate: Int? = null,
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
    reportCurrencies: ReportCurrenciesViewModel = hiltViewModel(),
    themeSettings: ThemeSettingsViewModel = hiltViewModel(),
) {
    val shakeEnabled by shakeSettings.enabled.collectAsStateWithLifecycle()
    val currencies by reportCurrencies.state.collectAsStateWithLifecycle()
    val themeMode by themeSettings.mode.collectAsStateWithLifecycle()
    val palette by themeSettings.palette.collectAsStateWithLifecycle()
    // Theme and palette open as a circle from the tapped control (design-refactor Step 4).
    val reveal = LocalThemeReveal.current
    SettingsScreen(
        state = SettingsState(
            themeMode = themeMode ?: ThemeMode.SYSTEM,
            palette = palette ?: ThemePalette.VOLT,
            mainCurrency = currencies.currencies.firstOrNull(),
            shakeEnabled = shakeEnabled,
            availableUpdate = availableUpdate,
        ),
        actions = SettingsActions(
            onBack = onBack,
            onThemeModeChange = { mode -> if (mode != themeMode) reveal.change { themeSettings.setMode(mode) } },
            onPaletteChange = { p -> if (p != palette) reveal.change { themeSettings.setPalette(p) } },
            onReportCurrencies = onReportCurrencies,
            onShakeEnabledChange = shakeSettings::setEnabled,
            onUpdate = onUpdate,
            onWipeData = onWipeData,
        ),
    )
}

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ThemePalette = ThemePalette.VOLT,
    val mainCurrency: String? = null,
    val shakeEnabled: Boolean = true,
    /** Build number of a newer release, shown under «Обновление приложения». */
    val availableUpdate: Int? = null,
)

data class SettingsActions(
    val onBack: () -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onPaletteChange: (ThemePalette) -> Unit = {},
    val onReportCurrencies: () -> Unit = {},
    val onShakeEnabledChange: (Boolean) -> Unit = {},
    val onUpdate: () -> Unit = {},
    /** «Данные → Стереть все данные» (board 18). */
    val onWipeData: () -> Unit = {},
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
            item { PaletteRow(selected = state.palette, onSelect = actions.onPaletteChange) }
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
                CoShellSwitchRow(
                    leading = { Icon(painterResource(DesignR.drawable.ic_mobile_vibrate), contentDescription = null) },
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
                    subtitle = state.availableUpdate?.let { stringResource(R.string.update_settings_available, it) },
                    onClick = actions.onUpdate,
                    opensScreen = true,
                )
            }
            item { SectionTitle(stringResource(R.string.settings_data_section)) }
            item { WipeDataRow(actions.onWipeData) }
        }
    }
}

/** The only red line in settings: it opens the confirmation, nothing is deleted here. */
@Composable
private fun WipeDataRow(onClick: () -> Unit) {
    val danger = MaterialTheme.colorScheme.error
    CoShellListRow(
        title = { Text(stringResource(R.string.wipe_title), color = danger, fontWeight = FontWeight.SemiBold) },
        supporting = { Text(stringResource(R.string.wipe_settings_hint)) },
        leading = { Icon(painterResource(DesignR.drawable.ic_delete), contentDescription = null, tint = danger) },
        trailing = { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) },
        onClick = onClick,
    )
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
