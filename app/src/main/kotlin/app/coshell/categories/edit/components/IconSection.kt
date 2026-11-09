package app.coshell.categories.edit.components

import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.components.CoShellSection

/** Grid of all category icons drawn in the chosen [color]; the selected one gets a ring. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IconSection(selected: String?, color: Long?, onSelect: (String) -> Unit) {
    // Same shade as the badge in this theme (neon in dark, deeper tone in light).
    val ring = color?.let { categoryColor(it) } ?: accentInk()
    CoShellSection(stringResource(R.string.category_icon)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
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
                        .then(if (isSelected) Modifier.border(Sizes.selectionRing, ring, CircleShape) else Modifier),
                )
            }
        }
    }
}
