package app.coshell.core.domain.access

import kotlin.time.Duration.Companion.days

/** What this phone keeps about its code: [revoked] — the server said it no longer works. */
data class StoredAccess(val code: String, val label: String, val checkedAtMs: Long, val revoked: Boolean = false)

/** What the server says about one code. */
sealed interface CodeLookup {
    /** The code works; [devices] — installations already using it, [maxDevices] — the limit. */
    data class Valid(val label: String, val devices: List<String>, val maxDevices: Int) : CodeLookup
    data class Revoked(val label: String) : CodeLookup
    data object NotFound : CodeLookup
}

/** Codes on the server (Firestore in `core:data`). Calls throw on network errors. */
interface AccessRemote {
    suspend fun lookup(code: String): CodeLookup

    /** Adds [installationId] to the code's devices; `false` — refused (the limit is reached, or the code was revoked meanwhile). */
    suspend fun addDevice(code: String, installationId: String): Boolean
}

/** Where the phone keeps its code (on the device only: not synced, not in the backup). */
interface AccessStore {
    suspend fun load(): StoredAccess?
    suspend fun save(access: StoredAccess)
    /** Random id of this installation (the same one the feedback form sends). */
    suspend fun installationId(): String
}

/** Result of typing a code. */
sealed interface EnterResult {
    data class Granted(val label: String) : EnterResult
    /** Cannot be a code: wrong length or characters. */
    data object Malformed : EnterResult
    data object NotFound : EnterResult
    data object Revoked : EnterResult
    /** Already on every phone the code allows. */
    data object Full : EnterResult
}

/**
 * The invitation gate (#666, docs/research/closed-testing-plan.md). A code is checked online once, then kept on the phone;
 * an online re-check every [RECHECK] only learns whether it was revoked — offline the app never closes by itself
 * (it is offline-first), only the server's word does.
 */
class AccessGate(
    private val remote: AccessRemote,
    private val store: AccessStore,
    private val nowMs: () -> Long,
) {
    /** Checks [typed] and, if it works, remembers it. Network errors are thrown. */
    suspend fun enter(typed: String): EnterResult {
        val code = AccessCode.normalize(typed) ?: return EnterResult.Malformed
        when (val found = remote.lookup(code)) {
            CodeLookup.NotFound -> return EnterResult.NotFound
            is CodeLookup.Revoked -> return EnterResult.Revoked
            is CodeLookup.Valid -> {
                val id = store.installationId()
                if (id !in found.devices) {
                    if (found.devices.size >= found.maxDevices || !remote.addDevice(code, id)) return EnterResult.Full
                }
                store.save(StoredAccess(code, found.label, nowMs()))
                return EnterResult.Granted(found.label)
            }
        }
    }

    /** Is it time to ask the server again about [stored]? */
    fun recheckDue(stored: StoredAccess?): Boolean =
        stored != null && !stored.revoked && nowMs() - stored.checkedAtMs >= RECHECK.inWholeMilliseconds

    /** Asks the server about the stored code; returns what the phone keeps now. Network errors keep it as it was. */
    suspend fun recheck(): StoredAccess? {
        val stored = store.load() ?: return null
        if (stored.revoked) return stored
        val next = try {
            when (val found = remote.lookup(stored.code)) {
                is CodeLookup.Valid -> stored.copy(label = found.label, checkedAtMs = nowMs())
                is CodeLookup.Revoked -> stored.copy(revoked = true)
                CodeLookup.NotFound -> stored.copy(revoked = true)
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            return stored
        }
        store.save(next)
        return next
    }

    companion object {
        val RECHECK = 3.days
    }
}
