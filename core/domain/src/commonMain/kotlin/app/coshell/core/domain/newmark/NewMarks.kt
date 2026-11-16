package app.coshell.core.domain.newmark

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Small per-phone state of the «новое» marks (#335): each person sees their own, nothing is synced or backed up.
 * SharedPreferences on Android.
 */
interface NewMarkStore {
    /** False until [NewMarks] runs for the first time on this phone. */
    suspend fun isStarted(): Boolean
    suspend fun setStarted()

    /** When [key] was first shown as new here; null if never. */
    suspend fun shownSince(key: String): Instant?
    suspend fun setShownSince(key: String, at: Instant)

    /** The place was opened (or never needs a mark): its mark is gone for good. */
    suspend fun isOpened(key: String): Boolean
    suspend fun setOpened(key: String)
}

/**
 * The «новое» pill next to a place with something new (#335, board `Fb335A`). The places are a list of keys that ships
 * with the app version; a key is marked from the first time this phone sees it, for [LIFETIME] or until the place is
 * opened. On a fresh install nothing is new — every key already listed is put away silently; an update (the app was
 * here before) shows the keys it brings.
 */
class NewMarks(
    private val store: NewMarkStore,
    private val clock: Clock,
    /** The app was installed before this version (Android: first install earlier than the last update). */
    private val wasUpdated: Boolean,
) {
    /** Which of [keys] wear the mark now; starts the clock for the keys seen for the first time. */
    suspend fun current(keys: Collection<String>): Set<String> {
        if (!store.isStarted()) {
            store.setStarted()
            if (!wasUpdated) {
                keys.forEach { store.setOpened(it) }
                return emptySet()
            }
        }
        val now = clock.now()
        return keys.filterTo(LinkedHashSet()) { key ->
            if (store.isOpened(key)) return@filterTo false
            val since = store.shownSince(key) ?: now.also { store.setShownSince(key, it) }
            now - since < LIFETIME
        }
    }

    /** The place behind [key] was opened: the mark goes away. */
    suspend fun opened(key: String) = store.setOpened(key)

    companion object {
        val LIFETIME: Duration = 7.days
    }
}
