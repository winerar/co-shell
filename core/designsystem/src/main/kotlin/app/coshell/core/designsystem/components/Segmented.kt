package app.coshell.core.designsystem.components

import app.coshell.core.designsystem.theme.selectionColors
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactile
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Single-choice segments (К3): a pill-shaped filled track with equal segments and one pill that slides to the
 * selected one on the spatial spring. The pill is chosen like everything (board Style1, «Единый стиль» А — also for
 * «how a list is shown», no violet): neon + 2 dp frame in ink + bold; the control is a 44 dp cell. The pill is drawn behind the labels, so a change only redraws — no
 * relayout (performance.md). The track itself can be swiped; to swipe the content under it as well, give
 * the content [segmentSwipe] with the same [swipe] — the pill then follows the finger (#173).
 * With [icons] (one per label), a track too narrow for any label shows the icons instead — all at once, the label stays for
 * TalkBack (#321: the entry sheet's type row shares its width with three round buttons).
 */
@Composable
fun CoShellSegmented(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    swipe: SegmentSwipe? = null,
    @DrawableRes icons: List<Int>? = null,
) {
    val iconsOrNull = icons?.takeIf { it.size == labels.size }
    val measurer = rememberTextMeasurer()
    val style = segmentStyle(selected = true)
    // The widest label (bold, as when chosen): a segment narrower than it shows the icon. Segments are equal, so all switch at once.
    val widest = remember(labels, style, iconsOrNull) {
        if (iconsOrNull == null) 0 else labels.maxOfOrNull { measurer.measure(it, style, maxLines = 1).size.width } ?: 0
    }
    val colors = MaterialTheme.colorScheme
    val sel = selectionColors()
    val pill = sel.fill
    val edge = fillEdge(pill, colors.surfaceContainerHigh)
    val ink = sel.content
    val position by animateFloatAsState(selected.toFloat(), Motion.spatial(), label = "segmentPill")
    val count = labels.size.coerceAtLeast(1)
    val ownSwipe = rememberSegmentSwipe()
    val pillSwipe = swipe ?: ownSwipe
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .background(colors.surfaceContainerHigh, PillShape)
            .swipeGesture(pillSwipe, selected, labels.size, onSelect, enabled, lean = false)
            .padding(Sizes.segmentInset)
            .drawBehind {
                val width = size.width / count
                val at = (position + pillSwipe.pillShift).coerceIn(0f, (count - 1).toFloat())
                drawRoundRect(
                    color = pill,
                    topLeft = Offset(width * at, 0f),
                    size = Size(width, size.height),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
                drawFillEdge(edge, Offset(width * at, 0f), Size(width, size.height), size.height / 2f)
            }
            .selectableGroup(),
    ) {
        labels.forEachIndexed { index, label ->
            Segment(label, iconsOrNull?.get(index), widest, index == selected, enabled, ink, colors.onSurfaceVariant, Modifier.weight(1f)) { onSelect(index) }
        }
    }
}

@Composable
private fun Segment(label: String, @DrawableRes icon: Int?, widest: Int, selected: Boolean, enabled: Boolean, ink: Color, muted: Color, modifier: Modifier, onClick: () -> Unit) {
    val color by animateColorAsState(if (selected) ink else muted, Motion.effects(), label = "segmentInk")
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(Sizes.segmentHeight)
            .tactile(source, Tactile.Control, PillShape, enabled)
            .selectable(selected, enabled = enabled, interactionSource = source, indication = null, role = Role.Tab, onClick = onClick)
            .padding(horizontal = Spacing.s),
        contentAlignment = Alignment.Center,
    ) {
        if (icon == null) {
            Text(text = label, color = color, style = segmentStyle(selected), maxLines = 1, overflow = TextOverflow.Ellipsis)
        } else {
            Layout(
                content = {
                    Text(text = label, color = color, style = segmentStyle(selected), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Icon(painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(Sizes.buttonIcon))
                },
                // TalkBack hears the label whichever of the two is placed.
                modifier = Modifier.clearAndSetSemantics { contentDescription = label },
            ) { measurables, constraints ->
                val shown = measurables[if (widest > constraints.maxWidth) 1 else 0].measure(constraints.copy(minWidth = 0, minHeight = 0))
                layout(shown.width, shown.height) { shown.place(0, 0) }
            }
        }
    }
}

@Composable
private fun segmentStyle(selected: Boolean): TextStyle =
    MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)

/** A locked control (the type of a category already in use) stays readable but clearly out of reach. */
private const val DISABLED_ALPHA = 0.5f
