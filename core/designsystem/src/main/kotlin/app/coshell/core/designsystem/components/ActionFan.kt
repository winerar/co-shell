package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import app.coshell.core.designsystem.theme.selectionColors
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import kotlinx.coroutines.withTimeoutOrNull

/** One button of the fan over «+» (#623): its [icon], the [label] under it and what happens when the finger lets go over it. */
@Immutable
data class FanAction(@DrawableRes val icon: Int, val label: String, val onSelect: () -> Unit)

/**
 * The fan over a «+» (#623, poll 2026-11-23 «А», board `EwHoldA`): hold [content] — round buttons open on an arc over it; without lifting,
 * lead the finger to one and let go: its action runs; letting go anywhere else closes the fan. A short tap goes through to [content] as before.
 * The hold takes the touch (the release is consumed), so the «+» does not also fire. [enabled] off — the fan never opens.
 * For TalkBack the [actions] are custom actions of the whole cell (the drag is out of reach there).
 */
@Composable
fun CoShellActionFan(actions: List<FanAction>, enabled: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val radius = with(density) { Sizes.fanRadius.toPx() }
    val reach = with(density) { Sizes.fanButton.toPx() } * REACH
    var open by remember { mutableStateOf(false) }
    var hovered by remember { mutableStateOf<Int?>(null) }
    val current by rememberUpdatedState(actions)
    val shown = animateFloatAsState(if (open) 1f else 0f, Motion.spatial(), label = "fan")
    Box(
        modifier
            .semantics { if (enabled) customActions = actions.map { a -> CustomAccessibilityAction(a.label) { a.onSelect(); true } } }
            .pointerInput(enabled, actions.size) {
                if (!enabled || actions.isEmpty()) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    if (endedBeforeHold(down.id, down.position, viewConfiguration.touchSlop, viewConfiguration.longPressTimeoutMillis)) return@awaitEachGesture
                    open = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val origin = Offset(size.width / 2f, size.height / 2f)
                    try {
                        while (true) {
                            val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: break
                            hovered = ActionFanGeometry.hit(change.position - origin, current.size, radius, reach)
                            change.consume()
                            if (!change.pressed) {
                                hovered?.let { current[it].onSelect() }
                                break
                            }
                        }
                    } finally {
                        open = false
                        hovered = null
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
        if (shown.value > MIN_SHOWN) {
            Popup(popupPositionProvider = FanPosition, properties = PopupProperties(focusable = false, clippingEnabled = false)) {
                FanButtons(actions, hovered, shown)
            }
        }
    }
}

/** `true` when the finger lifted or wandered off before the hold time passed: an ordinary tap or swipe, not the fan. */
private suspend fun AwaitPointerEventScope.endedBeforeHold(id: PointerId, from: Offset, slop: Float, holdMillis: Long): Boolean =
    withTimeoutOrNull(holdMillis) {
        var ended = false
        while (!ended) {
            val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == id }
            ended = change == null || change.changedToUp() || (change.position - from).getDistance() > slop
        }
        true
    } == true

/** The popup's bottom-centre sits on the centre of the «+»: the buttons' offsets are measured from there (`ActionFanGeometry`). */
private object FanPosition : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset =
        IntOffset(anchorBounds.center.x - popupContentSize.width / 2, anchorBounds.center.y - popupContentSize.height)
}

@Composable
private fun FanButtons(actions: List<FanAction>, hovered: Int?, shown: androidx.compose.runtime.State<Float>) {
    val density = LocalDensity.current
    val radius = with(density) { Sizes.fanRadius.toPx() }
    val height = Sizes.fanRadius + Sizes.fanButton / 2
    val width = Sizes.fanRadius * 2 + Sizes.fanItemWidth
    val widthPx = with(density) { width.toPx() }
    val heightPx = with(density) { height.toPx() }
    Box(
        Modifier
            .size(width, height)
            .graphicsLayer {
                scaleX = shown.value
                scaleY = shown.value
                alpha = shown.value.coerceIn(0f, 1f)
                transformOrigin = TransformOrigin(0.5f, 1f)
            },
    ) {
        actions.forEachIndexed { index, action ->
            val at = ActionFanGeometry.center(index, actions.size, radius)
            FanItem(
                action,
                on = hovered == index,
                // The item's top-left: its button centred on the arc point, the box centred on the popup's bottom-centre.
                modifier = Modifier.offset {
                    IntOffset(
                        (widthPx / 2f + at.x - Sizes.fanItemWidth.toPx() / 2f).toInt(),
                        (heightPx + at.y - Sizes.fanButton.toPx() / 2f).toInt(),
                    )
                },
            )
        }
    }
}

@Composable
private fun FanItem(action: FanAction, on: Boolean, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val chosen = selectionColors()
    val fill = if (on) chosen.fill else scheme.surfaceContainerHigh
    Column(modifier.width(Sizes.fanItemWidth), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Box(
            Modifier
                .size(Sizes.fanButton)
                .background(fill, CircleShape)
                .fillEdge(fill, CircleShape, scheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(action.icon), contentDescription = null, tint = if (on) chosen.content else scheme.onSurface)
        }
        Text(
            action.label,
            Modifier.background(scheme.surfaceContainerHigh, PillShape).padding(horizontal = Spacing.s, vertical = Spacing.xxs),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/** A finger within this many button widths of a button's centre counts as on it (a thumb is wider than a button). */
private const val REACH = 0.8f

/** Below this the fan is gone: the popup leaves the composition. */
private const val MIN_SHOWN = 0.01f
