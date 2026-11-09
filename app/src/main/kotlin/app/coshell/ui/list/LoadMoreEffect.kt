package app.coshell.ui.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Calls [onLoadMore] when the last visible item comes within [threshold] items of the end of a paged
 * list (`GrowingWindow`). Reads layout info in a snapshot flow, so scrolling causes no recompositions.
 */
@Composable
fun LoadMoreEffect(state: LazyListState, enabled: Boolean, onLoadMore: () -> Unit, threshold: Int = 30) {
    val currentOnLoadMore = rememberUpdatedState(onLoadMore)
    LaunchedEffect(state, enabled) {
        if (!enabled) return@LaunchedEffect
        snapshotFlow {
            val info = state.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@snapshotFlow false
            last >= info.totalItemsCount - 1 - threshold
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { currentOnLoadMore.value() }
    }
}
