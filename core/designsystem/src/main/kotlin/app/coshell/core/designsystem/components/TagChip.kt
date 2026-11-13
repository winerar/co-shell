package app.coshell.core.designsystem.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A tag to switch on and off (#293, boards `Fb129A`, `Fb129Pick`): a coloured dot and the name. On — the pill is
 * tinted with the tag's [color] (a stored palette value) like [CoShellTagPill]; off — a quiet outline.
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
    Row(
        modifier
            .tactileClickable(Tactile.Control, PillShape, role = Role.Checkbox, onClick = onClick)
            .selectionPop(selected)
            .widthIn(max = TagChipMaxWidth)
            .height(TagChipHeight)
            .background(if (selected) ink.copy(alpha = FillAlpha) else Color.Transparent, PillShape)
            .border(Sizes.hairline, if (selected) ink.copy(alpha = BorderAlpha) else colors.outlineVariant, PillShape)
            .padding(horizontal = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Box(Modifier.size(DotSize).background(ink, CircleShape))
        Text(
            name,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) ink else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (removeLabel != null) {
            Icon(painterResource(R.drawable.ic_close), removeLabel, Modifier.size(CloseSize), tint = ink)
        }
    }
}

/** «Все 34» after the tag pills: a dashed accent pill that opens the full list (board `Fb129A`). */
@Composable
fun CoShellTagMoreChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ink = accentInk()
    Box(
        modifier
            .tactileClickable(Tactile.Control, PillShape, role = Role.Button, onClick = onClick)
            .height(TagChipHeight)
            .dashedBorder(ink.copy(alpha = BorderAlpha), TagChipHeight)
            .padding(horizontal = Spacing.m),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = ink, maxLines = 1)
    }
}

private val TagChipHeight = 34.dp
private val TagChipMaxWidth = 180.dp
private val DotSize = 8.dp
private val CloseSize = 14.dp
private const val FillAlpha = 0.18f
private const val BorderAlpha = 0.5f
