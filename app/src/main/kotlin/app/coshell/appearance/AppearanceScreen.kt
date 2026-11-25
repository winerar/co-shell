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
import app.coshell.core.designsystem.components.underBars
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
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellSegmented
import androidx.compose.foundation.layout.fillMaxWidth
import app.coshell.settings.ListDensityViewModel
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.motion.LocalThemeReveal
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.theme.components.PaletteGrid
import app.coshell.theme.components.ThemeModePicker
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/**
 * «Настройки → Внешний вид» (board Set1Look): language (#352), theme, the nine colour themes in one grid (1.0, #354) —
 * the preferences of this phone, not of the family. Shake and reminders moved to their own groups (`settings/`).
 */
@Composable
fun AppearanceRoute(
    onBack: () -> Unit,
    themeSettings: ThemeSettingsViewModel = hiltViewModel(),
    listDensity: ListDensityViewModel = hiltViewModel(),
) {
    val themeMode by themeSettings.mode.collectAsStateWithLifecycle()
    val palette by themeSettings.palette.collectAsStateWithLifecycle()
    val compactLists by listDensity.compact.collectAsStateWithLifecycle()
    // Theme and palette open as a circle from the tapped control (design-refactor Step 4).
    val reveal = LocalThemeReveal.current
    val context = LocalContext.current
    var languageOpen by rememberSaveable { mutableStateOf(false) }
    val language = remember(languageOpen) { LanguageRow(AppLanguage.chosen(context), AppLanguage.current()) }
    AppearanceScreen(
        state = AppearanceState(themeMode ?: ThemeMode.SYSTEM, palette ?: ThemePalette.LIME, language, compactLists),
        actions = AppearanceActions(
            onBack = onBack,
            onLanguageClick = { languageOpen = true },
            onCompactListsChange = listDensity::setCompact,
            onThemeModeChange = { mode -> if (mode != themeMode) reveal.change { themeSettings.setMode(mode) } },
            onPaletteChange = { p -> if (p != palette) reveal.change { themeSettings.setPalette(p) } },
        ),
    )
    if (languageOpen) LanguageSettingsSheet(onClosed = { languageOpen = false })
}

/** «Язык» row: [chosen] `null` = follows the phone; [inEffect] — the language the app speaks now. */
data class LanguageRow(val chosen: String? = null, val inEffect: String = "ru")

data class AppearanceState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ThemePalette = ThemePalette.LIME,
    val language: LanguageRow = LanguageRow(),
    /** «Плотность списков»: compact rows in «Операциях» and «Счетах». */
    val compactLists: Boolean = false,
)

data class AppearanceActions(
    val onBack: () -> Unit = {},
    val onLanguageClick: () -> Unit = {},
    val onCompactListsChange: (Boolean) -> Unit = {},
    val onThemeModeChange: (ThemeMode) -> Unit = {},
    val onPaletteChange: (ThemePalette) -> Unit = {},
)

@Composable
fun AppearanceScreen(state: AppearanceState, actions: AppearanceActions) {
    Scaffold(
        topBar = { ScreenTopBar(stringResource(R.string.appearance_title), onBack = actions.onBack) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = underBars(padding)) {
            item { SectionTitle(stringResource(R.string.language_title)) }
            item { LanguageRowGroup(state.language, actions.onLanguageClick) }
            item { SectionTitle(stringResource(R.string.theme_title)) }
            item { ThemeModePicker(selected = state.themeMode, onSelect = actions.onThemeModeChange) }
            item { PaletteGrid(selected = state.palette, onSelect = actions.onPaletteChange, modifier = Modifier.padding(bottom = Spacing.l)) }
            item { SectionTitle(stringResource(R.string.density_section)) }
            item {
                CoShellSegmented(
                    labels = listOf(stringResource(R.string.density_comfortable), stringResource(R.string.density_compact)),
                    selected = if (state.compactLists) 1 else 0,
                    onSelect = { actions.onCompactListsChange(it == 1) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l),
                )
            }
            item { CoShellHint(stringResource(R.string.density_hint), Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)) }
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
                title = stringResource(R.string.language_title),
                subtitle = if (language.chosen == null) stringResource(R.string.language_row_phone, name) else name,
                onClick = onClick,
            )
        }
    }
}

/** Blue of the palette. */


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
