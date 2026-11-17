package app.coshell.core.domain.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** The period «Отчёты» open on. */
enum class ReportsStart {
    MONTH,
    QUARTER,
    YEAR,
    ;

    companion object {
        /** Saved as [name]; unknown or missing → [MONTH]. */
        fun parse(stored: String?): ReportsStart = entries.firstOrNull { it.name == stored } ?: MONTH
    }
}

/**
 * How the screens look on this phone — «Настройки → Экраны» (boards `Set1Ops`, `Set1Reports`, `Set1Accounts`). Each phone has
 * its own; the defaults are what the app showed before the options existed.
 */
data class ScreenOptions(
    /** «Операции»: the account line, the tag pills and the note under the category. */
    val opsShowAccount: Boolean = true,
    val opsShowTags: Boolean = true,
    val opsShowNote: Boolean = true,
    /** «Счета»: grouped by currency instead of type, and the archive shown at the start. */
    val accountsByCurrency: Boolean = false,
    val accountsShowArchived: Boolean = false,
    /** «Отчёты»: the period they open on and whether the previous period is compared. */
    val reportsStart: ReportsStart = ReportsStart.MONTH,
    val reportsCompare: Boolean = true,
)

/** [ScreenOptions] of this phone: read as a flow, changed as a whole (the caller copies what it flips). */
interface ScreenOptionsStore {
    val options: Flow<ScreenOptions>
    suspend fun update(change: (ScreenOptions) -> ScreenOptions)
}

/** In memory — for tests and previews. */
class InMemoryScreenOptions(initial: ScreenOptions = ScreenOptions()) : ScreenOptionsStore {
    val state = MutableStateFlow(initial)
    override val options: Flow<ScreenOptions> = state
    override suspend fun update(change: (ScreenOptions) -> ScreenOptions) {
        state.value = change(state.value)
    }
}
