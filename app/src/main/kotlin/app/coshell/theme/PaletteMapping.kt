package app.coshell.theme

import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.domain.settings.ThemePalette

/** The saved palette setting → the design system's palette. */
fun ThemePalette.toDesign(): CoShellPalette = when (this) {
    ThemePalette.LIME -> CoShellPalette.Lime
    ThemePalette.GARNET -> CoShellPalette.Garnet
    ThemePalette.BANANA -> CoShellPalette.Banana
    ThemePalette.MATE -> CoShellPalette.Mate
    ThemePalette.ESPRESSO -> CoShellPalette.Espresso
    ThemePalette.KVAS -> CoShellPalette.Kvas
    ThemePalette.SAKURA -> CoShellPalette.Sakura
    ThemePalette.CORNFLOWER -> CoShellPalette.Cornflower
    ThemePalette.LOTUS -> CoShellPalette.Lotus
}
