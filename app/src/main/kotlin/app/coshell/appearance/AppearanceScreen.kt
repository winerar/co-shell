package app.coshell.appearance

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.coshell.language.components.endonymRes
import app.coshell.language.LanguageSettingsSheet
import app.coshell.language.AppLanguage
import app.coshell.core.designsystem.components.CoShellMenuRow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.motion.LocalThemeReveal
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.recurring.notify.ReminderSettingsViewModel
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.theme.components.PaletteGrid
import app.coshell.theme.components.ThemeModePicker
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/**
 * «Ещё → Внешний вид» (board Fb154Look, #155; the former «Настройки» without the items that moved to «Ещё»):
 * language (#352), theme, the nine colour themes in one grid (1.0, #354), the shake gesture and payment reminders (#405) —
 * the preferences of this phone, not of the family.
 */
@Composable
fun AppearanceRoute(
    onBack: () -> Unit,
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
    themeSettings: ThemeSettingsViewModel = hiltViewModel(),
    reminderSettings: ReminderSettingsViewModel = hiltViewModel(),
) {
    val shakeEnabled by shakeSettings.enabled.collectAsStateWithLifecycle()
    val remindersEnabled by reminderSettings.enabled.collectAsStateWithLifecycle()
    val themeMode by themeSettings.mode.collectAsStateWithLifecycle()
    val palette by themeSettings.palette.collectAsStateWithLifecycle()
    // Theme and palette open as a circle from the tapped control (design-refactor Step 4).
    val reveal = LocalThemeReveal.current
    val context = LocalContext.current
    var languageOpen by rememberSaveable { mutableStateOf(false) }
    val language = remember(languageOpen) { LanguageRow(AppLanguage.chosen(context), AppLanguage.current()) }
    AppearanceScreen(
        state = AppearanceState(themeMode ?: ThemeMode.SYSTEM, palette ?: ThemePalette.LIME, shakeEnabled, language, remindersEnabled),
        actions = AppearanceActions(
            onBack = onBack,
            onLanguageClick = { languageOpen = true },
            onThemeModeChange = { mode -> if (mode != themeMode) reveal.change { themeSettings.setMode(mode) } },
            onPaletteChange = { p -> if (p != palette) reveal.change { themeSettings.setPalette(p) } },
            onShakeEnabledChange = shakeSettings::setEnabled,
            onRemindersEnabledChange = reminderSettings::setEnabled,
        ),
    )
    if (languageOpen) LanguageSettingsSheet(onClosed = { languageOpen = false })
}

/** «Язык» row: [chosen] `null` = follows the phone; [inEffect] — the language the app speaks now. */
data class LanguageRow(val chosen: String? = null, val inEffect: String = "ru")

data class AppearanceState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ThemePalette = ThemePalette.LIME,
    val shakeEnabled: Boolean = true,
    val language: LanguageRow = LanguageRow(),
    /** «Напоминания о платежах» (#405). */
    val remindersEnabled: Boolean = true,
)

data class AppearanceActions(
    val onBack: () -> Unit = {},
    val onLanguageClick: () -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onPaletteChange: (ThemePalette) -> Unit = {},
    val onShakeEnabledChange: (Boolean) -> Unit = {},
    val onRemindersEnabledChange: (Boolean) -> Unit = {},
)

@Composable
fun AppearanceScreen(state: AppearanceState, actions: AppearanceActions) {
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.appearance_title), onBack = actions.onBack) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { SectionTitle(stringResource(R.string.language_title)) }
            item { LanguageRowGroup(state.language, actions.onLanguageClick) }
            item { SectionTitle(stringResource(R.string.theme_title)) }
            item { ThemeModePicker(selected = state.themeMode, onSelect = actions.onThemeModeChange) }
            item { CoShellHint(stringResource(R.string.palette_hint), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
            item { PaletteGrid(selected = state.palette, onSelect = actions.onPaletteChange, modifier = Modifier.padding(bottom = Spacing.l)) }
            item { SectionTitle(stringResource(R.string.appearance_input_section)) }
            item {
                CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                    row {
                        CoShellSwitchRow(
                            leading = { MenuIcon(DesignR.drawable.ic_mobile_vibrate, categoryColor(CategoryPalette.colors[SHAKE_COLOR])) },
                            title = stringResource(R.string.shake_to_add_title),
                            subtitle = stringResource(R.string.shake_to_add_hint),
                            checked = state.shakeEnabled,
                            onCheckedChange = actions.onShakeEnabledChange,
                        )
                    }
                }
            }
            item { SectionTitle(stringResource(R.string.appearance_notifications_section)) }
            item {
                CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
                    row {
                        CoShellSwitchRow(
                            leading = { MenuIcon(DesignR.drawable.ic_refresh, categoryColor(CategoryPalette.colors[REMINDERS_COLOR])) },
                            title = stringResource(R.string.reminders_title),
                            subtitle = stringResource(R.string.reminders_hint),
                            checked = state.remindersEnabled,
                            onCheckedChange = actions.onRemindersEnabledChange,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageRowGroup(language: LanguageRow, onClick: () -> Unit) {
    val name = stringResource(endonymRes(language.inEffect))
    CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.l)) {
        row {
            CoShellMenuRow(
                icon = DesignR.drawable.ic_language,
                color = categoryColor(CategoryPalette.colors[LANGUAGE_COLOR]),
                title = stringResource(R.string.language_title),
                subtitle = if (language.chosen == null) stringResource(R.string.language_row_phone, name) else name,
                onClick = onClick,
            )
        }
    }
}

/** Blue of the palette. */
private const val LANGUAGE_COLOR = 14

/** Lime-green of the palette, as the «+» it opens. */
private const val SHAKE_COLOR = 8

/** The colour of «Регулярные платежи» in «Ещё». */
private const val REMINDERS_COLOR = 14

@Preview(showBackground = true)
@Composable
private fun AppearanceScreenPreview() {
    CoShellTheme { AppearanceScreen(AppearanceState(), AppearanceActions()) }
}

@Preview(showBackground = true)
@Composable
private fun AppearanceScreenDarkPreview() {
    CoShellTheme(darkTheme = true) { AppearanceScreen(AppearanceState(ThemeMode.DARK, ThemePalette.KVAS), AppearanceActions()) }
}
