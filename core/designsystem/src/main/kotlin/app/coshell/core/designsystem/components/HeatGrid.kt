package app.coshell.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import app.coshell.core.designsystem.motion.dataSwap
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.tokens.Motion
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import app.coshell.core.designsystem.category.inkOn
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.themeEdge
import app.coshell.core.designsystem.tokens.Radii
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One day of [CoShellHeatGrid] (#553). [level] 0 — nothing spent, 1..4 — brighter the more; [quiet] — a day that has not come
 * yet; [description] is what TalkBack reads («18 ноября, потрачено 44 USD»).
 */
data class HeatCell(
    val label: String,
    val level: Int,
    val description: String,
    val today: Boolean = false,
    val quiet: Boolean = false,
)

/**
 * «Календарь трат» (#553, board `CalendarA`): a month in weeks, [weekdays] on top, [leadingBlanks] empty places before the first day,
 * each day brighter by [HeatCell.level]; the neon of the theme with a hairline (`fillEdge`) where it melts into the card, today — a ring
 * of the theme ([themeEdge]), the [selected] day — a thicker one. Cells are 48 dp — a thumb-size target.
 */
@Composable
fun CoShellHeatGrid(
    weekdays: List<String>,
    leadingBlanks: Int,
    cells: List<HeatCell>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val columns = weekdays.size
    val places = List(leadingBlanks) { null } + cells.indices.map { it }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            weekdays.forEach {
                Text(
                    it,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        places.chunked(columns).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                week.forEach { index ->
                    if (index == null) Box(Modifier.weight(1f).height(Sizes.heatCell))
                    else HeatCellBox(cells[index], index == selected, { onSelect(index) }, Modifier.weight(1f))
                }
                repeat(columns - week.size) { Box(Modifier.weight(1f).height(Sizes.heatCell)) }
            }
        }
    }
}

@Composable
private fun HeatCellBox(cell: HeatCell, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val surface = MaterialTheme.colorScheme.surface
    val target = heatFill(cell.level, quiet = cell.quiet).compositeOver(surface)
    // Another month or another day picked: the cells change colour and the ring grows to its day instead of blinking (owner 2026-11-27).
    val fill by animateColorAsState(target, Motion.effects(), label = "heatFill")
    val shape = RoundedCornerShape(Radii.medium)
    val ringWidth by animateDpAsState(
        when {
            selected -> Sizes.heatSelectedRing
            cell.today -> Sizes.heatTodayRing
            else -> 0.dp
        },
        Motion.spatialFast(),
        label = "heatRing",
    )
    val ring = if (ringWidth > 0.dp) Modifier.border(ringWidth, themeEdge(), shape) else Modifier.fillEdge(target, shape, surface)
    Box(
        modifier
            .height(Sizes.heatCell)
            .background(fill, shape)
            .then(ring)
            .semantics { this.selected = selected; contentDescription = cell.description }
            .tactileClickable(Tactile.Control, shape, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            cell.label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected || cell.today) FontWeight.Bold else FontWeight.Normal,
            color = if (cell.quiet) MaterialTheme.colorScheme.onSurfaceVariant else inkOn(target),
            modifier = Modifier.alpha(if (cell.quiet) QUIET_ALPHA else 1f),
        )
    }
}

/**
 * The legend under the grid: «меньше ▪▪▪▪ больше» and [trailing] on the right («без трат: 4 дня»).
 */
@Composable
fun CoShellHeatLegend(lessLabel: String, moreLabel: String, trailing: String, modifier: Modifier = Modifier) {
    val surface = MaterialTheme.colorScheme.surface
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(lessLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        (1..HEAT_LEVELS).forEach { level ->
            val fill = heatFill(level, quiet = false).compositeOver(surface)
            Box(Modifier.size(Sizes.heatSwatch).background(fill, RoundedCornerShape(Radii.small)).fillEdge(fill, RoundedCornerShape(Radii.small), surface))
        }
        Text(moreLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            AnimatedContent(trailing, transitionSpec = { dataSwap() }, contentAlignment = Alignment.CenterEnd, label = "heatTrailing") {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
            }
        }
    }
}

private const val HEAT_LEVELS = 4
private const val QUIET_ALPHA = 0.55f

/** The fill of a level: a day without spending is a faint tint of the second colour, 1..4 — more and more of the main neon. */
@Composable
private fun heatFill(level: Int, quiet: Boolean): Color {
    val colors = MaterialTheme.colorScheme
    if (quiet) return Color.Transparent
    return when (level) {
        0 -> colors.secondary.copy(alpha = 0.10f)
        1 -> colors.primary.copy(alpha = 0.18f)
        2 -> colors.primary.copy(alpha = 0.38f)
        3 -> colors.primary.copy(alpha = 0.62f)
        else -> colors.primary
    }
}
