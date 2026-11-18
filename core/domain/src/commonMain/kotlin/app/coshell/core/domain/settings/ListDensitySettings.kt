package app.coshell.core.domain.settings

import kotlinx.coroutines.flow.Flow

/** «Плотность списков» of «Операций» and «Счетов»: comfortable rows (default) or compact ones. Device-only. */
interface ListDensitySettings {
    val compact: Flow<Boolean>
    suspend fun setCompact(compact: Boolean)
}
