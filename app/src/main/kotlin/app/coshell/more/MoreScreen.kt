package app.coshell.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.currencies.list.ReportCurrenciesViewModel
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.theme.components.ThemeModeRow
import app.coshell.ui.list.ActionRow
import app.coshell.ui.list.SwitchRow
import app.coshell.core.designsystem.R as DesignR

// Menu tab: links to categories, exchange rates, report currencies, family sync, backup, app update and feedback, plus the theme and shake-to-add settings.

@Composable
fun MoreRoute(
    onCategories: () -> Unit,
    onRates: () -> Unit,
    onReportCurrencies: () -> Unit,
    onFamily: () -> Unit,
    onBackup: () -> Unit,
    onUpdate: () -> Unit,
    onFeedback: () -> Unit,
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
    reportCurrencies: ReportCurrenciesViewModel = hiltViewModel(),
    themeSettings: ThemeSettingsViewModel = hiltViewModel(),
) {
    val shakeEnabled by shakeSettings.enabled.collectAsStateWithLifecycle()
    val currencies by reportCurrencies.state.collectAsStateWithLifecycle()
    val themeMode by themeSettings.mode.collectAsStateWithLifecycle()
    MoreScreen(
        onCategories = onCategories,
        onRates = onRates,
        onReportCurrencies = onReportCurrencies,
        mainCurrency = currencies.currencies.firstOrNull(),
        onFamily = onFamily,
        onBackup = onBackup,
        onUpdate = onUpdate,
        onFeedback = onFeedback,
        shakeEnabled = shakeEnabled,
        onShakeEnabledChange = shakeSettings::setEnabled,
        themeMode = themeMode ?: ThemeMode.SYSTEM,
        onThemeModeChange = themeSettings::setMode,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onCategories: () -> Unit,
    onRates: () -> Unit,
    onReportCurrencies: () -> Unit,
    mainCurrency: String?,
    onFamily: () -> Unit,
    onBackup: () -> Unit,
    onUpdate: () -> Unit,
    onFeedback: () -> Unit,
    shakeEnabled: Boolean,
    onShakeEnabledChange: (Boolean) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_more)) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_category,
                    title = stringResource(R.string.categories_title),
                    onClick = onCategories,
                    opensScreen = true,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_currency_exchange,
                    title = stringResource(R.string.rates_title),
                    subtitle = stringResource(R.string.rates_menu_hint),
                    onClick = onRates,
                    opensScreen = true,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_payments,
                    title = stringResource(R.string.report_currencies_title),
                    subtitle = mainCurrency?.let { stringResource(R.string.report_currencies_menu_hint, it) },
                    onClick = onReportCurrencies,
                    opensScreen = true,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_group,
                    title = stringResource(R.string.family_title),
                    subtitle = stringResource(R.string.family_menu_hint),
                    onClick = onFamily,
                    opensScreen = true,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_settings_backup_restore,
                    title = stringResource(R.string.backup_title),
                    onClick = onBackup,
                    opensScreen = true,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_deployed_code_update,
                    title = stringResource(R.string.update_title),
                    onClick = onUpdate,
                    opensScreen = true,
                )
            }
            item {
                ActionRow(
                    icon = DesignR.drawable.ic_feedback,
                    title = stringResource(R.string.feedback_title),
                    subtitle = stringResource(R.string.feedback_menu_hint),
                    onClick = onFeedback,
                    opensScreen = true,
                )
            }
            item { ThemeModeRow(selected = themeMode, onSelect = onThemeModeChange) }
            item {
                SwitchRow(
                    icon = DesignR.drawable.ic_mobile_vibrate,
                    title = stringResource(R.string.shake_to_add_title),
                    subtitle = stringResource(R.string.shake_to_add_hint),
                    checked = shakeEnabled,
                    onCheckedChange = onShakeEnabledChange,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MoreScreenPreview() {
    CoShellTheme { 
        MoreScreen(
            onCategories = {},
            onRates = {},
            onReportCurrencies = {},
            mainCurrency = "USD",
            onFamily = {},
            onBackup = {},
            onUpdate = {},
            onFeedback = {},
            shakeEnabled = true,
            onShakeEnabledChange = {},
            themeMode = ThemeMode.SYSTEM,
            onThemeModeChange = {},
        )
     }
}
