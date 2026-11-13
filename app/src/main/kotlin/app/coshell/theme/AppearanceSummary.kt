package app.coshell.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.theme.components.labelRes

/** «Лайм · тёмная» — the line under «Ещё → Внешний вид». */
@Composable
fun appearanceSummary(mode: ThemeMode, palette: ThemePalette): String =
    stringResource(R.string.appearance_menu_hint, stringResource(palette.labelRes), stringResource(mode.labelRes).lowercase())
