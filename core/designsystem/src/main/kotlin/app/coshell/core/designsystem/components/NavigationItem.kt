package app.coshell.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactile
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Bottom bar tab as in the mockups: an icon, the whole cell is the target ([width] — the bar gives the selected tab more,
 * its neighbour less, `BarWidths`). [pillModifier] marks the slot the active-tab pill fills (`TabPill.kt`); the tab sinks
 * under the finger, its icon pops when it opens. The selected tab also shows its [label] under the icon when [showLabel]
 * (the bar decides from the final width, so the word never flickers while the pill grows, #796); TalkBack reads [label] always.
 * No dash under the selected tab (owner 2026-11-18, #511).
 */
@Composable
internal fun CoShellBarTab(
    label: String,
    selected: Boolean,
    showLabel: Boolean,
    labelColor: Color,
    width: Dp,
    onClick: () -> Unit,
    pillModifier: Modifier,
    icon: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val named by animateFloatAsState(if (selected && showLabel) 1f else 0f, Motion.effects(), label = "tabLabel")
    Box(
        Modifier
            .width(width)
            .height(Sizes.bottomBarHeight)
            .tactile(source, Tactile.Control, PillShape)
            .selectable(selected, interactionSource = source, indication = null, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xxs)
                .then(pillModifier)
                .height(Sizes.bottomBarPill),
            contentAlignment = Alignment.Center,
        ) {
            // The icon rises a little while the name appears under it; only drawing changes, no layout.
            Box(Modifier.offset { IntOffset(0, -(NAME_LIFT.roundToPx() * named).toInt()) }.selectionPop(selected)) { icon() }
            Text(
                label,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.xs)
                    .alpha(named)
                    .clearAndSetSemantics { },
                color = labelColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

private val NAME_LIFT = Spacing.s
