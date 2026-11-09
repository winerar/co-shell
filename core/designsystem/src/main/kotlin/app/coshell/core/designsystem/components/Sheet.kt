package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.Animatable
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import app.coshell.core.designsystem.motion.PressOrigin
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.growFrom
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Spacing
import kotlinx.coroutines.launch

// Bottom sheets (design-system.md §«Навигация и жесты»): editors slide up over the screen and close with
// a swipe down, a tap outside or the back gesture; 28 dp top corners (shapes.extraLarge). A sheet grows out of
// the element that opened it (issue #63): from the «+» it also starts in the «+»'s colour (`motion/GrowFrom`).

/** Window of a `dialog<…>` navigation destination that hosts a [CoShellSheet]: full screen, insets left to the sheet. */
val CoShellSheetDialogProperties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)

/**
 * A bottom sheet that is open while it is in composition — put it in a `dialog<…>` destination with
 * [CoShellSheetDialogProperties] (its ViewModel and arguments then work as for a screen). [onClosed]
 * fires exactly once, after a swipe away or after `close`; [content] gets `close` — slide down, then [onClosed].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoShellSheet(onClosed: () -> Unit, content: @Composable (close: () -> Unit) -> Unit) {
    // The dialog window must not dim the screen too: the sheet draws its own scrim.
    val view = LocalView.current
    LaunchedEffect(view) { (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val once = remember { CloseOnce(onClosed) }
    val close: () -> Unit = { scope.launch { sheetState.hide() }.invokeOnCompletion { once.close() } }

    // Opened by a tap → grow out of it while sliding up; opened otherwise (shake) → only the slide.
    val from = remember { PressOrigin.take() }
    val growth = remember { Animatable(if (from == null) 1f else 0f) }
    LaunchedEffect(Unit) { growth.animateTo(1f, Motion.spatial()) }
    val grow = if (from != null) Modifier.growFrom(from, { growth.value }, BottomSheetDefaults.ExpandedShape) else Modifier

    ModalBottomSheet(onDismissRequest = once::close, sheetState = sheetState, modifier = grow) {
        content(close)
    }
}

/**
 * Inside of a sheet: [title], scrolling form, and the main action ([actionLabel], «Сохранить») pinned at
 * the bottom across the width — under the thumb and above the keyboard. No back arrow.
 */
@Composable
fun CoShellSheetLayout(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
    showAction: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.imePadding()) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = Spacing.l).padding(bottom = Spacing.s),
        )
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.l, vertical = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
            content = content,
        )
        if (showAction) {
            CoShellButton(
                onClick = onAction,
                enabled = actionEnabled,
                tactile = Tactile.Primary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.m),
            ) { Text(actionLabel) }
        }
    }
}

/** Fires [onClosed] once, whether the sheet was swiped away or closed after saving. */
internal class CloseOnce(private val onClosed: () -> Unit) {
    private var closed = false

    fun close() {
        if (closed) return
        closed = true
        onClosed()
    }
}
