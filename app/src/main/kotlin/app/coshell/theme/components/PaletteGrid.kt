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
import app.coshell.core.domain.settings.PaletteGroup
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.theme.toDesign

/** «Внешний вид»: the themes of one [group] as swatches, four in a row (board Fb271A, #271). */
@Composable
fun PaletteGrid(group: PaletteGroup, selected: ThemePalette, onSelect: (ThemePalette) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        ThemePalette.entries.filter { it.group == group }.chunked(PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
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

private const val PER_ROW = 4

@get:StringRes
val PaletteGroup.titleRes: Int
    get() = when (this) {
        PaletteGroup.JUICY -> R.string.palette_group_juicy
        PaletteGroup.SOFT -> R.string.palette_group_soft
    }

@get:StringRes
internal val ThemePalette.labelRes: Int
    get() = when (this) {
        ThemePalette.LIME -> R.string.palette_lime
        ThemePalette.PASSIONFRUIT -> R.string.palette_passionfruit
        ThemePalette.MANGO -> R.string.palette_mango
        ThemePalette.BILBERRY -> R.string.palette_bilberry
        ThemePalette.FEIJOA -> R.string.palette_feijoa
        ThemePalette.PITAYA -> R.string.palette_pitaya
        ThemePalette.LEMON -> R.string.palette_lemon
        ThemePalette.PEACH -> R.string.palette_peach
        ThemePalette.LYCHEE -> R.string.palette_lychee
        ThemePalette.MELON -> R.string.palette_melon
        ThemePalette.BLUEBERRY -> R.string.palette_blueberry
        ThemePalette.PLUM -> R.string.palette_plum
        ThemePalette.PEAR -> R.string.palette_pear
        ThemePalette.GOOSEBERRY -> R.string.palette_gooseberry
    }
