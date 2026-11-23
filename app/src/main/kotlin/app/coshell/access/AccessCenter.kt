package app.coshell.access

import app.coshell.core.domain.access.AccessGate
import app.coshell.core.domain.access.AccessStore
import app.coshell.core.domain.access.EnterResult
import app.coshell.core.domain.access.StoredAccess
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Whether this build asks for an invitation code (release builds with `ACCESS_GATE=true`, #666); everywhere else the app is open. */
class AccessEnabled(val on: Boolean)

/** Where the phone stands with its invitation code. */
sealed interface AccessState {
    /** The code is being read from the phone (a few ms): nothing is drawn yet, so the code screen never flashes. */
    data object Loading : AccessState
    /** This build has no gate. */
    data object Open : AccessState
    /** No code yet: the code screen. */
    data object Required : AccessState
    data class Granted(val label: String) : AccessState
    /** The server closed the code: entry is locked, data stays (`gate/InputLock`). */
    data class Revoked(val label: String) : AccessState
}

/** Why a typed code was not accepted. */
enum class AccessError { MALFORMED, NOT_FOUND, REVOKED, FULL, NETWORK }

/** The code screen: [busy] while the server is asked, [error] after a refusal. */
data class AccessEntry(val busy: Boolean = false, val error: AccessError? = null)

/**
 * The invitation gate of the app (#666): reads the code, checks a typed one, asks the server again every few days
 * ([AccessGate.RECHECK]) and says whether entry is closed. One for the whole process.
 */
class AccessCenter(private val gate: AccessGate, private val store: AccessStore, private val enabled: AccessEnabled) {
    private val _state = MutableStateFlow<AccessState>(if (enabled.on) AccessState.Loading else AccessState.Open)
    val state: StateFlow<AccessState> = _state.asStateFlow()

    private val _entry = MutableStateFlow(AccessEntry())
    val entry: StateFlow<AccessEntry> = _entry.asStateFlow()

    /** The person asked to type another code (from the «Доступ закрыт» window): the code screen over the app. */
    private val _entering = MutableStateFlow(false)
    val entering: StateFlow<Boolean> = _entering.asStateFlow()

    /** The «Доступ закрыт» window: on opening a closed app and after a blocked tap. */
    private val _note = MutableStateFlow(false)
    val note: StateFlow<Boolean> = _note.asStateFlow()

    /** Reads the code from the phone and, if it is time, asks the server about it. Only the first call reads. */
    suspend fun start() {
        if (_state.value != AccessState.Loading) return
        val stored = store.load()
        apply(stored)
        if (stored?.revoked == true) _note.value = true
        recheck()
    }

    /** Asks the server about the code when [AccessGate.RECHECK] has passed; closes entry if the code was revoked. */
    suspend fun recheck() {
        if (!enabled.on || _state.value is AccessState.Loading) return
        if (!gate.recheckDue(store.load())) return
        val next = gate.recheck()
        apply(next)
        if (next?.revoked == true) _note.value = true
    }

    /** Checks [typed]; on success the app opens. */
    suspend fun submit(typed: String) {
        _entry.value = AccessEntry(busy = true)
        val error = try {
            when (val result = gate.enter(typed)) {
                is EnterResult.Granted -> {
                    _state.value = AccessState.Granted(result.label)
                    _entering.value = false
                    _note.value = false
                    null
                }
                EnterResult.Malformed -> AccessError.MALFORMED
                EnterResult.NotFound -> AccessError.NOT_FOUND
                EnterResult.Revoked -> AccessError.REVOKED
                EnterResult.Full -> AccessError.FULL
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AccessError.NETWORK
        }
        _entry.value = AccessEntry(error = error)
    }

    fun changeCode() { _entry.value = AccessEntry(); _note.value = false; _entering.value = true }

    fun cancelEntering() { _entering.value = false; _entry.update { it.copy(error = null) } }

    fun showNote() { _note.value = true }

    fun dismissNote() { _note.value = false }

    private fun apply(stored: StoredAccess?) {
        if (!enabled.on) return
        _state.value = when {
            stored == null -> AccessState.Required
            stored.revoked -> AccessState.Revoked(stored.label)
            else -> AccessState.Granted(stored.label)
        }
    }
}
