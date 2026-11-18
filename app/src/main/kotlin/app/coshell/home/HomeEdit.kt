package app.coshell.home

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.HomeCard
import app.coshell.core.domain.settings.HomeLayout
import kotlinx.coroutines.withTimeoutOrNull
import app.coshell.core.designsystem.R as DesignR

// Editing «Обзор» right on the screen (board Set1HomeEdit): hold a card, then ⠿ drags it and ✕ hides it; «Готово» ends it.
// The same order and visibility as «Настройки → Экраны → Обзор» (`HomeLayout`).

/** A long press anywhere on the card starts the editing; the card itself still gets its taps (the press is only watched). */
internal fun Modifier.longPressToEdit(enabled: Boolean, onLongPress: () -> Unit): Modifier = if (!enabled) this else composed {
    val timeout = LocalViewConfiguration.current.longPressTimeoutMillis
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val up = withTimeoutOrNull(timeout) { waitForUpOrCancellation(PointerEventPass.Initial) }
            if (up == null) onLongPress()
        }
    }
}

/** The line over a card in the editing mode: its name, ⠿ to drag, ✕ to hide. */
@Composable
internal fun CardEditHeader(card: HomeCard, onHide: () -> Unit, dragModifier: Modifier, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(stringResource(card.titleRes()), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        CoShellIconButton(onClick = {}, modifier = dragModifier) {
            Icon(painterResource(DesignR.drawable.ic_drag_indicator), contentDescription = stringResource(R.string.settings_home_drag))
        }
        CoShellIconButton(onClick = onHide) {
            Icon(painterResource(DesignR.drawable.ic_close), contentDescription = stringResource(R.string.home_edit_hide))
        }
    }
}

/**
 * The drag of one card: it follows the finger, and on release takes the place of the card it was dragged over (judged by the
 * real heights of the neighbours in [listState]). [onMove] gets the new [HomeLayout].
 */
@Composable
internal fun rememberCardDrag(card: HomeCard, layout: HomeLayout, listState: LazyListState, onMove: (HomeLayout) -> Unit): CardDrag {
    val drag = remember(card) { CardDrag() }
    drag.modifier = Modifier.pointerInput(card, layout) {
        detectDragGestures(
            onDragStart = { drag.offset = 0f },
            onDrag = { change, amount -> change.consume(); drag.offset += amount.y },
            onDragEnd = {
                onMove(layout.moveBy(card, drag.offset, listState))
                drag.offset = 0f
            },
            onDragCancel = { drag.offset = 0f },
        )
    }
    return drag
}

internal class CardDrag {
    var offset by mutableFloatStateOf(0f)
    var modifier: Modifier = Modifier

    /** Applied to the card block while it is dragged. */
    val shift: Modifier get() = Modifier.graphicsLayer { translationY = offset }
}

/** The layout after [card] was dragged by [offset] px, judged by the real heights of the cards in [listState]. */
internal fun HomeLayout.moveBy(card: HomeCard, offset: Float, listState: LazyListState): HomeLayout {
    val sizes = listState.layoutInfo.visibleItemsInfo.mapNotNull { info -> HomeCard.entries.firstOrNull { it.name == info.key }?.let { it to info.size.toFloat() } }.toMap()
    return moveBy(card, offset, sizes)
}

/** Pure part of [moveBy]: past a neighbour once more than half of its [sizes] entry is covered; unknown sizes stop the move. */
internal fun HomeLayout.moveBy(card: HomeCard, offset: Float, sizes: Map<HomeCard, Float>): HomeLayout {
    val shown = visible
    val from = shown.indexOf(card)
    if (from < 0 || sizes.isEmpty()) return this
    var target = from
    var remaining = offset
    fun size(index: Int) = sizes[shown[index]] ?: Float.MAX_VALUE
    while (remaining > 0 && target < shown.lastIndex && remaining > size(target + 1) / 2) {
        remaining -= size(target + 1)
        target++
    }
    while (remaining < 0 && target > 0 && -remaining > size(target - 1) / 2) {
        remaining += size(target - 1)
        target--
    }
    if (target == from) return this
    return move(order.indexOf(card), order.indexOf(shown[target]))
}

/** The name of a card in the editing mode and in «Настройки». */
internal fun HomeCard.titleRes(): Int = when (this) {
    HomeCard.REVIEW -> R.string.settings_home_review
    HomeCard.UPCOMING -> R.string.settings_home_upcoming
    HomeCard.TILES -> R.string.settings_home_tiles
    HomeCard.HOT -> R.string.settings_home_hot
    HomeCard.RECENT -> R.string.settings_home_recent
}
