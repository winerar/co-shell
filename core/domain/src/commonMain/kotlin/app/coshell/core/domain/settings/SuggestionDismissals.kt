package app.coshell.core.domain.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Suggestions the person turned down — «Это разные» of a duplicate (#791), «Не сейчас» of a regular payment (#789): kept on the
 * phone, not synced, like hints. Keys carry their own prefix (`dup:`, `recurring:`).
 */
interface SuggestionDismissals {
    val dismissed: Flow<Set<String>>

    suspend fun dismiss(key: String)
}

/** For previews and tests. */
class InMemorySuggestionDismissals(initial: Set<String> = emptySet()) : SuggestionDismissals {
    private val state = MutableStateFlow(initial)
    override val dismissed: Flow<Set<String>> = state

    override suspend fun dismiss(key: String) = state.update { it + key }
}
