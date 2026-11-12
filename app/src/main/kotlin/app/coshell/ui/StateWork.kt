package app.coshell.ui

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * Where screens build their UiState (#258). `combine { … }.stateIn(viewModelScope)` would run the
 * transform on the main thread — rates parsing, totals, grouping and formatting of thousands of rows
 * would then cost frames. Tests swap [context] for an empty one (`MainDispatcherRule`) to stay synchronous.
 */
object StateWork {
    @Volatile var context: CoroutineContext = Dispatchers.Default
}

/** Everything upstream (Room mapping, combine transforms) runs off the main thread; read when the flow is built. */
fun <T> Flow<T>.offMain(): Flow<T> = flowOn(StateWork.context)
