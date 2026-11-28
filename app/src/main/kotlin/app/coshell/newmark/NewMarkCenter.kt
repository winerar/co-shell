package app.coshell.newmark

import app.coshell.core.domain.newmark.NewMarkTree
import app.coshell.core.domain.newmark.NewMarks
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The «новое» marks of the whole app, read once per process (#335): «Ещё», «Настройки» and the screens inside see one state, so a
 * place opened deep inside drops its pill on the way back too. [marked] — the places themselves plus the ways to them
 * ([NewFeatures.inside], `NewMarkTree`).
 */
@Singleton
class NewMarkCenter @Inject constructor(private val marks: NewMarks) {
    private val own = MutableStateFlow<Set<String>>(emptySet())
    private val _marked = MutableStateFlow<Set<String>>(emptySet())
    val marked: StateFlow<Set<String>> = _marked.asStateFlow()

    private val lock = Mutex()
    private var loaded = false

    suspend fun load() = lock.withLock {
        if (loaded) return@withLock
        loaded = true
        set(marks.current(NewFeatures.tracked, NewFeatures.inherits).intersect(NewFeatures.all.toSet()))
    }

    /** The place behind [key] was opened: its mark goes away now and for good. A way ([NewFeatures.inside]) has none of its own. */
    suspend fun opened(key: String) {
        if (key in NewFeatures.inside || key !in own.value) return
        set(own.value - key)
        marks.opened(key)
    }

    private fun set(keys: Set<String>) {
        own.update { keys }
        _marked.value = NewMarkTree.marked(keys, NewFeatures.inside)
    }
}
