package app.coshell.theme.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellPaletteSwatch
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.theme.toDesign

/** «Внешний вид»: all nine themes as small screens, three in a row, one list (picker «А», owner 2026-11-16). */
@Composable
fun PaletteGrid(selected: ThemePalette, onSelect: (ThemePalette) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        ThemePalette.entries.chunked(PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                row.forEach { palette ->
                    CoShellPaletteSwatch(
                        palette = palette.toDesign(),
                        label = stringResource(palette.labelRes),
                        selected = palette == selected,
                        onClick = { onSelect(palette) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val PER_ROW = 3

@get:StringRes
internal val ThemePalette.labelRes: Int
    get() = when (this) {
        ThemePalette.LIME -> R.string.palette_lime
        ThemePalette.GARNET -> R.string.palette_garnet
        ThemePalette.BANANA -> R.string.palette_banana
        ThemePalette.MATE -> R.string.palette_mate
        ThemePalette.ESPRESSO -> R.string.palette_espresso
        ThemePalette.KVAS -> R.string.palette_kvas
        ThemePalette.SAKURA -> R.string.palette_sakura
        ThemePalette.CORNFLOWER -> R.string.palette_cornflower
        ThemePalette.LOTUS -> R.string.palette_lotus
    }
