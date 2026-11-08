package app.coshell.core.domain.shake

import kotlinx.coroutines.flow.Flow

/** The "shake to add a transaction" switch (More → settings). On by default; kept on the device only. */
interface ShakeSettings {
    val enabled: Flow<Boolean>
    suspend fun setEnabled(enabled: Boolean)
}
