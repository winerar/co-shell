package app.coshell.transactions.list

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Paging for a live Room list (ARCHITECTURE §9, 2026-11-09): observes the newest [limit] rows and grows the
 * window by [pageSize] when the user nears the end. Keeps sticky day headers and live updates simple;
 * memory grows only as far as the user scrolls.
 */
class GrowingWindow(private val pageSize: Int = PAGE_SIZE) {
    private val _limit = MutableStateFlow(pageSize)
    val limit: StateFlow<Int> = _limit.asStateFlow()

    /** Fewer rows than asked for means the window already holds everything. */
    private var canGrow = false

    /** [load] is re-subscribed with the new limit whenever the window grows. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> observe(load: (limit: Int) -> Flow<List<T>>): Flow<Page<T>> =
        _limit.flatMapLatest { limit ->
            load(limit).map { items ->
                canGrow = items.size >= limit
                Page(items, hasMore = canGrow)
            }
        }

    /** Called by the screen near the end of the list; repeated calls before the next page are no-ops. */
    fun loadMore() {
        if (!canGrow) return
        canGrow = false
        _limit.value += pageSize
    }

    data class Page<T>(val items: List<T>, val hasMore: Boolean)

    companion object {
        /** ~10 screens of rows: one Room query stays well under a frame budget. */
        const val PAGE_SIZE = 200
    }
}
