package app.coshell.categories.edit.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.ui.form.FormSection
import app.coshell.core.designsystem.R as DesignR

/** Palette swatches; the selected colour shows a check mark. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColorSection(selected: Long?, onSelect: (Long) -> Unit) {
    // A colour saved before the neon palette shows as its neon swatch.
    val selectedSwatch = selected?.let(CategoryPalette::normalize)
    FormSection(stringResource(R.string.category_color)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryPalette.colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .tactileClickable(Tactile.Control, CircleShape) { onSelect(color) }
                        .selectionPop(color == selectedSwatch)
                        .size(40.dp)
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
