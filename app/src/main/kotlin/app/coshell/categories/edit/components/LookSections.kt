package app.coshell.categories.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.ui.form.ColorStrip
import app.coshell.ui.form.IconStrip

/**
 * «Иконка» and «Цвет» (#290, board `Fb290FA`): one line each; «Все 52» and «+12» call [onOpenAll] — the shared
 * «Иконка и цвет» sheet the screen shows.
 */
@Composable
internal fun LookSections(icon: String?, color: Long?, onIconSelect: (String) -> Unit, onColorSelect: (Long) -> Unit, onOpenAll: () -> Unit) {
    CoShellFormSection(stringResource(R.string.category_icon)) {
        IconStrip(icon, color, onIconSelect, onAll = onOpenAll)
    }
    CoShellFormSection(stringResource(R.string.category_color)) {
        ColorStrip(color, { picked -> picked?.let(onColorSelect) }, onAll = onOpenAll)
    }
}
