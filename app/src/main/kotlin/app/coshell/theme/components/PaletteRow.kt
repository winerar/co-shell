package app.coshell.theme.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellPaletteCard
import app.coshell.core.designsystem.components.CoShellSection
import androidx.compose.foundation.layout.Column
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.theme.toDesign

/** Settings block «Палитра»: the six palettes as cards, two in a row (mockup 15). */
@Composable
fun PaletteRow(selected: ThemePalette, onSelect: (ThemePalette) -> Unit) {
    CoShellSection(stringResource(R.string.palette_title), modifier = Modifier.padding(horizontal = Spacing.l)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            ThemePalette.entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    pair.forEach { palette ->
                        CoShellPaletteCard(
                            palette = palette.toDesign(),
                            label = stringResource(palette.labelRes),
                            hint = stringResource(palette.hintRes),
                            selected = palette == selected,
                            onClick = { onSelect(palette) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@get:StringRes
private val ThemePalette.labelRes: Int
    get() = when (this) {
        ThemePalette.VOLT -> R.string.palette_volt
        ThemePalette.MOJITO -> R.string.palette_mojito
        ThemePalette.MANGO -> R.string.palette_mango
        ThemePalette.ULTRA -> R.string.palette_ultra
        ThemePalette.LAGOON -> R.string.palette_lagoon
        ThemePalette.SAKURA -> R.string.palette_sakura
    }

@get:StringRes
private val ThemePalette.hintRes: Int
    get() = when (this) {
        ThemePalette.VOLT -> R.string.palette_volt_hint
        ThemePalette.MOJITO -> R.string.palette_mojito_hint
        ThemePalette.MANGO -> R.string.palette_mango_hint
        ThemePalette.ULTRA -> R.string.palette_ultra_hint
        ThemePalette.LAGOON -> R.string.palette_lagoon_hint
        ThemePalette.SAKURA -> R.string.palette_sakura_hint
    }
