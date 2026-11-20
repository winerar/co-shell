package app.coshell.core.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * Which lists the family has put in its own order by dragging (#338). A family setting (#122): synced, the same on
 * every phone. Off — the list goes alphabetically (the default); on — by `sortOrder`.
 */
interface ManualOrderSettings {
    val manualAccounts: Flow<Boolean>
    val manualCategories: Flow<Boolean>

    suspend fun setManualAccounts()
    suspend fun setManualCategories()

    companion object {
        const val ON = "manual"

        fun parse(stored: String?): Boolean = stored?.trim() == ON
    }
}
