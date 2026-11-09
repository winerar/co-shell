package app.coshell.core.designsystem.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.tokens.Motion

// Size changes (issue #64, owner: "every element that changes its shape or size does it with an animation").
// One rule for the whole app: nothing jumps to a new size — it grows or shrinks with `Motion.Resize`.
// The design-system components already do it inside; screens use these helpers for their own blocks
// (`MotionRuleTest` keeps raw `AnimatedVisibility`/`animateContentSize`/`animateItem` out of screens).

/** The element follows its content's size smoothly (text got longer, a line appeared). Put it before padding. */
fun Modifier.smoothResize(): Modifier = animateContentSize(Motion.Resize)

/** Unfolding from the top (a drop-down part of a block): grows and fades in. */
val ExpandIn: EnterTransition = expandVertically(Motion.Resize, expandFrom = Alignment.Top) + fadeIn(Motion.effects())

/** Folding back: shrinks to the top and fades out a bit faster than it shrinks. */
val CollapseOut: ExitTransition = shrinkVertically(Motion.Resize, shrinkTowards = Alignment.Top) + fadeOut(Motion.effectsFast())

/**
 * A drop-down part of a block (details under a total, extra fields of a form): unfolds when [expanded],
 * folds back when not — the block around it follows the height instead of jumping. [content] is a column
 * with [spacing] between its children. Inside a `spacedBy` column, put the gap *inside* (`padding(top)`) and
 * keep the expandable in a column that always has something visible, or the parent's gap jumps.
 */
@Composable
fun CoShellExpandable(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(visible = expanded, modifier = modifier, enter = ExpandIn, exit = CollapseOut) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing), content = content)
    }
}

/**
 * Motion of a row in a lazy list: a new row (a folder opened, an operation added) fades in, a removed one
 * fades out, and the rest slide to their places with the same spring as [smoothResize]. Needs a `key`.
 */
fun LazyItemScope.listItemMotion(): Modifier = Modifier.animateItem(
    fadeInSpec = Motion.effects(),
    placementSpec = Motion.Placement,
    fadeOutSpec = Motion.effectsFast(),
)
