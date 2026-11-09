package app.coshell.theme

import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.domain.settings.ThemePalette

/** The saved palette setting → the design system's palette. */
fun ThemePalette.toDesign(): CoShellPalette = when (this) {
    ThemePalette.VOLT -> CoShellPalette.Volt
    ThemePalette.MOJITO -> CoShellPalette.Mojito
    ThemePalette.MANGO -> CoShellPalette.Mango
    ThemePalette.ULTRA -> CoShellPalette.Ultra
}
