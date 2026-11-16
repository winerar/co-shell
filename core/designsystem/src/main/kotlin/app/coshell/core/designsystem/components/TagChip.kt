package app.coshell.core.designsystem.components

import app.coshell.core.designsystem.theme.selectionColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A tag to switch on and off (#293): the same chip as every choice (board Style1) — on: neon + 2 dp frame in ink + bold,
 * off: the filled track; the tag's [color] (a stored palette value) lives only in the dot.
 * [removeLabel] — a «✕» after the name (the chosen tags on top of the «Теги» sheet); the whole pill still toggles.
 */
@Composable
fun CoShellTagChip(
    name: String,
    color: Long,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    removeLabel: String? = null,
) {
    val ink = categoryColor(color)
    val colors = MaterialTheme.colorScheme
    val sel = selectionColors()
    Row(
        modifier
            .tactileClickable(Tactile.Control, PillShape, role = Role.Checkbox, onClick = onClick)
            .selectionPop(selected)
            .widthIn(max = TagChipMaxWidth)
            .height(Sizes.chipHeight)
            .background(if (selected) sel.fill else colors.surfaceContainerHigh, PillShape)
            .padding(horizontal = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Box(Modifier.size(DotSize).background(ink, CircleShape))
        Text(
            name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) sel.content else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (removeLabel != null) {
            Icon(painterResource(R.drawable.ic_close), removeLabel, Modifier.size(CloseSize), tint = if (selected) sel.content else colors.onSurfaceVariant)
        }
    }
}

/** «Все 34» after the tag pills: an outline pill in ink that opens the full list (board Style1). */
@Composable
fun CoShellTagMoreChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier
            .tactileClickable(Tactile.Control, PillShape, role = Role.Button, onClick = onClick)
            .height(Sizes.chipHeight)
            .border(Sizes.hairline, colors.outline, PillShape)
            .padding(horizontal = Spacing.m),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onSurface, maxLines = 1)
    }
}

private val TagChipMaxWidth = 180.dp
private val DotSize = 8.dp
private val CloseSize = 14.dp
