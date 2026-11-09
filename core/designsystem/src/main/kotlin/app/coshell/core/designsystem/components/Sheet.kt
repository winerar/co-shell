package app.coshell.core.designsystem.components

import android.view.ViewGroup
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import app.coshell.core.designsystem.motion.PressOrigin
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.colorTintAlpha
import app.coshell.core.designsystem.motion.transformOriginIn
import app.coshell.core.designsystem.tokens.Spacing
import kotlin.coroutines.cancellation.CancellationException

// Bottom sheets (design-system.md §«Навигация и жесты», motion.md §«Шторка»): editors rise over the screen and close
// with a swipe down, a tap outside or the back gesture; 28 dp top corners. Our own sheet instead of Material's
// `ModalBottomSheet` (which opens a second window and animates on its own curve): one source of motion —
// `SheetMotionState.hidden` — drives position, scale, scrim and the opener's colour (`SheetMotion.kt`).

/** Window of a `dialog<…>` navigation destination that hosts a [CoShellSheet]: full screen, insets left to the sheet. */
val CoShellSheetDialogProperties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)

/**
 * A bottom sheet that is open while it is in composition — put it in a `dialog<…>` destination with
 * [CoShellSheetDialogProperties] (its ViewModel and arguments then work as for a screen). [onClosed]
 * fires exactly once, after a swipe away or after `close`; [content] gets `close` — slide down, then [onClosed].
 * Opened by a tap, it grows a little around the tap; from the «+» it starts in the «+»'s colour.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoShellSheet(onClosed: () -> Unit, content: @Composable (close: () -> Unit) -> Unit) {
    // The dialog window is only a stage: full screen, no dim and no window animation — the sheet does all of it.
    val view = LocalView.current
    LaunchedEffect(view) {
        (view.parent as? DialogWindowProvider)?.window?.apply {
            setDimAmount(0f)
            setWindowAnimations(0)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    val once = remember { CloseOnce(onClosed) }
    val scope = rememberCoroutineScope()
    val motion = remember { SheetMotionState(scope, once::close) }
    val close: () -> Unit = remember { { motion.close() } }
    val from = remember { PressOrigin.take() }
    LaunchedEffect(Unit) { motion.open() }

    // Predictive back: the sheet shrinks with the gesture; let go → it closes, cancel → springs back.
    PredictiveBackHandler(enabled = !motion.closing) { events ->
        try {
            events.collect { motion.back = it.progress }
            motion.close()
        } catch (e: CancellationException) {
            motion.cancelBack()
            throw e
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        // Scrim: a sibling under the sheet, so a tap on the sheet never reaches it.
        val scrim = BottomSheetDefaults.ScrimColor
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind { drawRect(scrim, alpha = scrimFraction(motion.hidden)) }
                .pointerInput(Unit) { detectTapGestures { motion.close() } },
        )
        val shape = BottomSheetDefaults.ExpandedShape
        var pivotX by remember { mutableFloatStateOf(TransformOrigin.Center.pivotFractionX) }
        val pivotTaken = remember { booleanArrayOf(false) }
        val drag = rememberDraggableState { motion.dragBy(it) }
        Surface(
            shape = shape,
            color = BottomSheetDefaults.ContainerColor,
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = Spacing.xl)
                .fillMaxWidth()
                .onSizeChanged { motion.heightPx = it.height.toFloat() }
                // The tap's place is taken once, before any movement — not re-read while the sheet flies.
                .onPlaced {
                    if (from != null && !pivotTaken[0]) {
                        pivotTaken[0] = true
                        pivotX = transformOriginIn(it.boundsInWindow(), from.position).pivotFractionX
                    }
                }
                .graphicsLayer {
                    translationY = motion.hidden * size.height
                    val scale = sheetScale(motion.hidden, motion.back)
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(pivotX, 1f)
                }
                .drawWithContent {
                    drawContent()
                    val color = from?.color
                    if (color == null || !color.isSpecified || !motion.tinting) return@drawWithContent
                    val tint = colorTintAlpha(1f - motion.hidden)
                    if (tint > 0f) drawOutline(shape.createOutline(size, layoutDirection, this), color.copy(alpha = tint))
                }
                .nestedScroll(remember { SheetNestedScroll(motion) })
                .draggable(drag, Orientation.Vertical, onDragStopped = { motion.settle(it) }),
        ) {
            Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))) {
                BottomSheetDefaults.DragHandle(Modifier.align(Alignment.CenterHorizontally))
                content(close)
            }
        }
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
