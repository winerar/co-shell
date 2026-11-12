package app.coshell.appearance

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
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.motion.LocalThemeReveal
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.theme.components.PaletteGrid
import app.coshell.theme.components.ThemeModePicker
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/**
 * «Ещё → Внешний вид» (board Fb154Look, #155; the former «Настройки» without the items that moved to «Ещё»):
 * theme, palette, and the shake gesture — the preferences of this phone, not of the family.
 */
@Composable
fun AppearanceRoute(
    onBack: () -> Unit,
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
    themeSettings: ThemeSettingsViewModel = hiltViewModel(),
) {
    val shakeEnabled by shakeSettings.enabled.collectAsStateWithLifecycle()
    val themeMode by themeSettings.mode.collectAsStateWithLifecycle()
    val palette by themeSettings.palette.collectAsStateWithLifecycle()
    // Theme and palette open as a circle from the tapped control (design-refactor Step 4).
    val reveal = LocalThemeReveal.current
    AppearanceScreen(
        state = AppearanceState(themeMode ?: ThemeMode.SYSTEM, palette ?: ThemePalette.VOLT, shakeEnabled),
        actions = AppearanceActions(
            onBack = onBack,
            onThemeModeChange = { mode -> if (mode != themeMode) reveal.change { themeSettings.setMode(mode) } },
            onPaletteChange = { p -> if (p != palette) reveal.change { themeSettings.setPalette(p) } },
            onShakeEnabledChange = shakeSettings::setEnabled,
        ),
    )
}

data class AppearanceState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ThemePalette = ThemePalette.VOLT,
    val shakeEnabled: Boolean = true,
)

data class AppearanceActions(
    val onBack: () -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onPaletteChange: (ThemePalette) -> Unit = {},
    val onShakeEnabledChange: (Boolean) -> Unit = {},
)

@Composable
fun AppearanceScreen(state: AppearanceState, actions: AppearanceActions) {
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.appearance_title), onBack = actions.onBack) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item { SectionTitle(stringResource(R.string.theme_title)) }
            item { ThemeModePicker(selected = state.themeMode, onSelect = actions.onThemeModeChange) }
            item { SectionTitle(stringResource(R.string.palette_title)) }
            item { PaletteGrid(selected = state.palette, onSelect = actions.onPaletteChange) }
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
        }
    }
}

/** Lime-green of the palette, as the «+» it opens. */
private const val SHAKE_COLOR = 9

@Preview(showBackground = true)
@Composable
private fun AppearanceScreenPreview() {
    CoShellTheme { AppearanceScreen(AppearanceState(), AppearanceActions()) }
}

@Preview(showBackground = true)
@Composable
private fun AppearanceScreenDarkPreview() {
    CoShellTheme(darkTheme = true) { AppearanceScreen(AppearanceState(ThemeMode.DARK, ThemePalette.MANGO), AppearanceActions()) }
}
