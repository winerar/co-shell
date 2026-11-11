package app.coshell.update.install

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

/**
 * Holds at most one value until someone takes it: a newer value replaces an untaken one, and
 * [take] hands it out exactly once (two collectors never both get it).
 */
class OneShotSlot<T : Any> {
    private val slot = MutableStateFlow<T?>(null)

    /** The waiting value, or null; collect it and call [take] to claim it. */
    val value: StateFlow<T?> = slot.asStateFlow()

    fun put(item: T) {
        slot.value = item
    }

    fun take(): T? = slot.getAndUpdate { null }
}
