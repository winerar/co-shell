package app.coshell.categories.edit.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.ui.form.FormSection

/** Grid of all category icons drawn in the chosen [color]; the selected one gets a ring. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IconSection(selected: String?, color: Long?, onSelect: (String) -> Unit) {
    val ring = color?.let(::Color) ?: MaterialTheme.colorScheme.primary
    FormSection(stringResource(R.string.category_icon)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryIcons.keys.forEach { key ->
                val isSelected = key == selected
                CategoryBadge(
                    icon = key,
                    color = color,
                    modifier = Modifier
                        .tactileClickable(Tactile.Control, CircleShape) { onSelect(key) }
                        .selectionPop(isSelected)
                        .clip(CircleShape)
                        .then(if (isSelected) Modifier.border(2.dp, ring, CircleShape) else Modifier),
                )
            }
        }
    }
}
