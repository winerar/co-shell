package app.coshell.theme.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellPaletteSwatch
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.theme.toDesign
import app.coshell.core.designsystem.R as DesignR

/** Settings line «Палитра»: four swatches (Вольт, Мохито, Манго, Ультра) and the chosen one's description. */
@Composable
fun PaletteRow(selected: ThemePalette, onSelect: (ThemePalette) -> Unit) {
    CoShellListRow(
        leading = { Icon(painterResource(DesignR.drawable.ic_contrast), contentDescription = null) },
        title = { Text(stringResource(R.string.palette_title)) },
        supporting = {
            Text(stringResource(selected.hintRes))
            Row(
                Modifier.fillMaxWidth().padding(top = Spacing.s),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ThemePalette.entries.forEach { palette ->
                    CoShellPaletteSwatch(
                        palette = palette.toDesign(),
                        label = stringResource(palette.labelRes),
                        selected = palette == selected,
                        onClick = { onSelect(palette) },
                    )
                }
            }
        },
    )
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
