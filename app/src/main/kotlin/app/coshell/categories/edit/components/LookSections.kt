package app.coshell.categories.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.ui.form.rememberColorLabel
import app.coshell.ui.form.rememberIconLabel
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.designsystem.components.CoShellColorStrip
import app.coshell.core.designsystem.components.CoShellIconStrip

/**
 * «Иконка» and «Цвет» (#290, board `Fb290FA`): one line each; «Все 52» and «+12» call [onOpenAll] — the shared
 * «Иконка и цвет» sheet the screen shows.
 */
@Composable
internal fun LookSections(icon: String?, color: Long?, onIconSelect: (String) -> Unit, onColorSelect: (Long) -> Unit, onOpenAll: () -> Unit) {
    CoShellFormSection(stringResource(R.string.category_icon)) {
        CoShellIconStrip(icon, onIconSelect, onAll = onOpenAll, allLabel = stringResource(R.string.form_all_count, CategoryIcons.keys.size), iconLabel = rememberIconLabel())
    }
    CoShellFormSection(stringResource(R.string.category_color)) {
        CoShellColorStrip(color, { picked -> picked?.let(onColorSelect) }, onAll = onOpenAll, noneLabel = stringResource(R.string.color_none), colorLabel = rememberColorLabel())
    }
}
