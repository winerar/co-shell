package app.coshell.ui.top

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.coshell.core.designsystem.tokens.Motion

/**
 * A tab header that gives way to [SelectionTopBar] while rows are chosen: the selection header slides down
 * from above over the tab header and slides back up when the mode ends (header poll А, 2026-11-15).
 */
@Composable
fun TabOrSelectionTopBar(
    title: String,
    scroll: TabScroll,
    selectedCount: Int,
    onClearSelection: () -> Unit,
    subtitle: String? = null,
) {
    // The leaving selection header keeps its last count instead of showing «Выбрано 0».
    var lastCount by remember { mutableIntStateOf(1) }
    if (selectedCount > 0) lastCount = selectedCount
    AnimatedContent(
        targetState = selectedCount > 0,
        transitionSpec = {
            if (targetState) {
                (slideInVertically(Motion.Placement) { -it } + fadeIn(Motion.effects()))
                    .togetherWith(fadeOut(Motion.effects()))
            } else {
                fadeIn(Motion.effects())
                    .togetherWith(slideOutVertically(Motion.Placement) { -it } + fadeOut(Motion.effects()))
            }.apply { targetContentZIndex = if (targetState) 1f else 0f }
        },
        label = "selectionHeader",
    ) { selecting ->
        if (selecting) SelectionTopBar(lastCount, onClearSelection, scroll)
        else TabTopBar(title, scroll, subtitle)
    }
}
