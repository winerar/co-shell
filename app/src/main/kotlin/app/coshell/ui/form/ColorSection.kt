package app.coshell.ui.form

import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.components.CoShellSection
import app.coshell.core.designsystem.R as DesignR

/**
 * Palette swatches of a category or an account; the selected colour shows a check mark.
 * [allowNone] — the first swatch means "no colour" (`onSelect(null)`), for accounts.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColorSection(selected: Long?, onSelect: (Long?) -> Unit, allowNone: Boolean = false) {
    // A colour saved before the neon palette shows as its neon swatch.
    val selectedSwatch = selected?.let(CategoryPalette::normalize)
    CoShellSection(stringResource(R.string.category_color)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            if (allowNone) {
                Box(
                    modifier = Modifier
                        .tactileClickable(Tactile.Control, CircleShape) { onSelect(null) }
                        .selectionPop(selected == null)
                        .size(Sizes.badge)
                        .border(Sizes.selectionRing, MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    val icon = if (selected == null) DesignR.drawable.ic_check else DesignR.drawable.ic_close
                    Icon(
                        painterResource(icon),
                        contentDescription = stringResource(R.string.color_none),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            CategoryPalette.colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .tactileClickable(Tactile.Control, CircleShape) { onSelect(color) }
                        .selectionPop(color == selectedSwatch)
                        .size(Sizes.badge)
                        .clip(CircleShape)
                        .background(categoryColor(color)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (color == selectedSwatch) {
                        Icon(painterResource(DesignR.drawable.ic_check), contentDescription = null, tint = MaterialTheme.colorScheme.surface)
                    }
                }
            }
        }
    }
}
