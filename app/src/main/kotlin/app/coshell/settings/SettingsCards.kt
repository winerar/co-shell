package app.coshell.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.components.RowGroupScope
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.WeekStart
import app.coshell.theme.components.ThemeModePicker
import app.coshell.theme.components.labelRes
import app.coshell.core.designsystem.R as DesignR

// The group cards of «Настройки» (boards SetCardsC, SetCardsC2): the screen — `SettingsScreen.kt`, the group screens —
// `SettingsGroups.kt`, `ScreensSettings.kt`. Each card holds what is changed most often; the rest is behind «Все ›».

@Composable
internal fun LookCard(state: SettingsState, actions: SettingsActions) =
    GroupCard(DesignR.drawable.ic_contrast, 18, R.string.appearance_title, actions.onLook) {
        row {
            Column(Modifier.padding(vertical = Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Text(stringResource(R.string.theme_title), Modifier.padding(horizontal = Spacing.l), style = MaterialTheme.typography.bodyLarge)
                ThemeModePicker(selected = state.themeMode, onSelect = actions.onThemeMode)
            }
        }
        row { ValueRow(stringResource(R.string.settings_palette), stringResource(state.palette.labelRes), actions.onLook) }
    }

@Composable
internal fun InputCard(state: SettingsState, actions: SettingsActions) =
    GroupCard(DesignR.drawable.ic_add, 8, R.string.settings_input_title, actions.onInput) {
        row {
            CoShellSwitchRow(
                title = stringResource(R.string.shake_to_add_title), subtitle = stringResource(R.string.shake_to_add_hint),
                checked = state.shake, onCheckedChange = actions.onShake,
            )
        }
        row {
            CoShellSwitchRow(
                title = stringResource(R.string.settings_input_haptics), subtitle = stringResource(R.string.settings_input_haptics_hint),
                checked = state.haptics, onCheckedChange = actions.onHaptics,
            )
        }
    }

@Composable
internal fun NoticesCard(state: SettingsState, actions: SettingsActions) =
    GroupCard(DesignR.drawable.ic_refresh, 14, R.string.settings_notices_title, actions.onNotices) {
        row {
            CoShellSwitchRow(
                title = stringResource(R.string.reminders_title), subtitle = stringResource(R.string.reminders_hint),
                checked = state.reminders, onCheckedChange = actions.onReminders,
            )
        }
        row { ValueRow(stringResource(R.string.settings_notices_time), stringResource(R.string.settings_notices_hour, state.reminderHour), actions.onNotices) }
    }

@Composable
internal fun MoneyCard(state: SettingsState, actions: SettingsActions) =
    GroupCard(DesignR.drawable.ic_payments, 17, R.string.settings_money_title, actions.onMoney) {
        row { ValueRow(stringResource(R.string.report_currencies_title), state.currencies, actions.onCurrencies) }
        row { ValueRow(stringResource(R.string.settings_money_week), stringResource(state.weekStart.labelRes), actions.onMoney) }
    }

@Composable
internal fun ScreensCard(actions: SettingsActions) =
    GroupCard(DesignR.drawable.ic_home, 8, R.string.settings_screens_title, actions.onScreens) {
        row { ValueRow(stringResource(R.string.settings_screens_row), stringResource(R.string.settings_screens_row_hint), actions.onScreens) }
    }

@Composable
internal fun DataCard(state: SettingsState, actions: SettingsActions) =
    GroupCard(DesignR.drawable.ic_archive, 5, R.string.settings_data_title, actions.onData) {
        row { ValueRow(stringResource(R.string.backup_title), stringResource(R.string.backup_menu_hint), actions.onBackup) }
        row {
            CoShellSwitchRow(
                title = stringResource(R.string.settings_data_wifi), subtitle = stringResource(R.string.settings_data_wifi_hint),
                checked = state.wifiOnly, onCheckedChange = actions.onWifiOnly,
            )
        }
    }

/** A group: its icon and name in caps, «Все ›» to the group screen [onAll], then its main settings in one card. */
@Composable
private fun GroupCard(@DrawableRes icon: Int, color: Int, @StringRes title: Int, onAll: () -> Unit, rows: RowGroupScope.() -> Unit) {
    Column(Modifier.padding(horizontal = Spacing.l).padding(top = Spacing.m, bottom = Spacing.s)) {
        Row(Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.s), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            MenuIcon(icon, categoryColor(CategoryPalette.colors[color]))
            CoShellSectionLabel(stringResource(title).uppercase(), Modifier.weight(1f))
            CoShellSectionLink(stringResource(R.string.settings_group_all), onClick = onAll)
        }
        CoShellRowGroup(rows = rows)
    }
}

/** A setting shown by its value: tap opens where it is chosen. */
@Composable
private fun ValueRow(title: String, value: String, onClick: () -> Unit) = CoShellListRow(
    title = title,
    subtitle = value,
    trailing = { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) },
    onClick = onClick,
    dense = true,
)

@get:StringRes
private val WeekStart.labelRes: Int
    get() = when (this) {
        WeekStart.MONDAY -> R.string.settings_money_week_mon
        WeekStart.SUNDAY -> R.string.settings_money_week_sun
        WeekStart.SYSTEM -> R.string.settings_money_week_system
    }
