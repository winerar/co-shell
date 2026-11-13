package app.coshell.theme

import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.domain.settings.ThemePalette

/** The saved palette setting → the design system's palette. */
fun ThemePalette.toDesign(): CoShellPalette = when (this) {
    ThemePalette.LIME -> CoShellPalette.Lime
    ThemePalette.PASSIONFRUIT -> CoShellPalette.Passionfruit
    ThemePalette.MANGO -> CoShellPalette.Mango
    ThemePalette.BILBERRY -> CoShellPalette.Bilberry
    ThemePalette.FEIJOA -> CoShellPalette.Feijoa
    ThemePalette.PITAYA -> CoShellPalette.Pitaya
    ThemePalette.LEMON -> CoShellPalette.Lemon
    ThemePalette.PEACH -> CoShellPalette.Peach
    ThemePalette.LYCHEE -> CoShellPalette.Lychee
    ThemePalette.MELON -> CoShellPalette.Melon
    ThemePalette.BLUEBERRY -> CoShellPalette.Blueberry
    ThemePalette.PLUM -> CoShellPalette.Plum
    ThemePalette.PEAR -> CoShellPalette.Pear
    ThemePalette.GOOSEBERRY -> CoShellPalette.Gooseberry
}
