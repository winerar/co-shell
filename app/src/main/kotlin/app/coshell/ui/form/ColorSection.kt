package app.coshell.ui.form

import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.components.CoShellColorDot
import app.coshell.core.designsystem.components.CoShellNoColorDot
import app.coshell.core.designsystem.components.CoShellSection

/**
 * Palette swatches of a category or an account; the selected colour shows a check mark.
 * [allowNone] — the first swatch means "no colour" (`onSelect(null)`), for accounts.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColorSection(selected: Long?, onSelect: (Long?) -> Unit, allowNone: Boolean = false) {
    // A colour saved before the neon palette shows as its neon swatch.
    val selectedSwatch = selected?.let(CategoryPalette::normalize)
    val label = rememberColorLabel()
    CoShellSection(stringResource(R.string.category_color)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            if (allowNone) CoShellNoColorDot(selected == null, stringResource(R.string.color_none)) { onSelect(null) }
            CategoryPalette.colors.forEach { color ->
                CoShellColorDot(color, color == selectedSwatch, label(color)) { onSelect(color) }
            }
        }
    }
}
