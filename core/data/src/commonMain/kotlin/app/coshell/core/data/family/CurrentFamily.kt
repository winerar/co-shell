package app.coshell.core.data.family

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Id of the family this phone syncs with (`null` — none). [FamilyService] sets it whenever the membership is
 * known; the Firestore store and the bell read it on every call, so a phone that leaves one family and joins
 * another needs no new objects (docs/ARCHITECTURE.md §10).
 */
class CurrentFamily(initial: String? = null) {
    private val _id = MutableStateFlow(initial)
    val flow: StateFlow<String?> = _id.asStateFlow()

    var id: String?
        get() = _id.value
        set(value) { _id.value = value }

    /** The id, or an error for code that only runs while in a family (sync). */
    fun require(): String = id ?: error("Not in a family")
}
