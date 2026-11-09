package app.coshell.core.data.auth

import app.coshell.core.data.remote.IdTokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

/** Signed out, or no ID token yet (sign-in needed) — sync is skipped, local work continues. */
class NotSignedInException : Exception("Not signed in")

/** What the rest of the app needs from sign-in; [AuthManager] is the real one, tests use a fake. */
interface AuthGateway : IdTokenProvider {
    val session: StateFlow<AuthSession?>
    val uid: String? get() = session.value?.uid
    suspend fun signInWithGoogle(googleIdToken: String): AuthSession
    fun signOut()
}

/**
 * Current Firebase session: sign-in, sign-out and an always-fresh ID token for REST calls
 * (refreshed 5 minutes before it expires). The session survives restarts via [store].
 */
class AuthManager(
    private val api: FirebaseAuthApi,
    private val store: SessionStore,
    private val clock: Clock = Clock.System,
) : AuthGateway {
    private val _session = MutableStateFlow(store.load())
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()
    private val mutex = Mutex()

    override suspend fun signInWithGoogle(googleIdToken: String): AuthSession {
        val session = api.signInWithGoogle(googleIdToken, now())
        update(session)
        return session
    }

    override fun signOut() = update(null)

    override suspend fun idToken(): String = mutex.withLock {
        val current = _session.value ?: throw NotSignedInException()
        if (current.expiresAtMs - now() > REFRESH_MARGIN_MS) return current.idToken
        val fresh = api.refresh(current, now())
        update(fresh)
        fresh.idToken
    }

    private fun update(session: AuthSession?) {
        store.save(session)
        _session.value = session
    }

    private fun now() = clock.now().toEpochMilliseconds()

    private companion object {
        const val REFRESH_MARGIN_MS = 5 * 60_000L
    }
}
