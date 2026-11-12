package app.coshell.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.family.FamilyService
import app.coshell.core.data.family.FamilyState
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.domain.family.isPlausibleEmail
import app.coshell.core.domain.family.normalizeEmail
import app.coshell.family.data.GoogleSignInResult
import app.coshell.ui.format.formatDayTime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

/** Sign-in, family creation and invitations, sync status (docs/ARCHITECTURE.md §10). */
@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val service: FamilyService,
    syncDao: SyncDao,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val local = MutableStateFlow(FamilyUiState())

    val state: StateFlow<FamilyUiState> = combine(
        local, service.state, syncDao.observePendingCount(), syncDao.observeState(SyncEngine.LAST_SYNC),
    ) { ui, family, pending, lastSync ->
        ui.copy(family = family, pending = pending, lastSync = lastSync?.toLongOrNull()?.let(::formatTime))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FamilyUiState(family = service.state.value))

    init {
        busy { service.load(); service.refresh(); loadInvites() }
    }

    fun onGoogleResult(result: GoogleSignInResult) = when (result) {
        is GoogleSignInResult.Success -> busy {
            runCatching { service.signIn(result.idToken) }.onFailure { message(FamilyMessage.SignInFailed) }
            loadInvites()
        }
        GoogleSignInResult.Cancelled -> Unit
        is GoogleSignInResult.Failed -> message(FamilyMessage.SignInFailed)
    }

    fun retry() = busy { service.refresh(); loadInvites() }

    fun createFamily() = busy {
        val created = network { service.createFamily() } ?: return@busy
        if (created) network { service.sync() } else message(FamilyMessage.FamilyExists)
    }

    /** First step of joining: ask about own data only if there is some. */
    fun join() = busy {
        if (service.hasLocalData()) local.update { it.copy(askJoinMode = true) } else doJoin(keepLocalData = false)
    }

    fun chooseJoinMode(keepLocalData: Boolean) {
        local.update { it.copy(askJoinMode = false) }
        busy { doJoin(keepLocalData) }
    }

    fun dismissJoinMode() = local.update { it.copy(askJoinMode = false) }

    fun invite(email: String) = busy {
        if (!isPlausibleEmail(email)) return@busy message(FamilyMessage.InvalidEmail)
        network { service.invite(email) } ?: return@busy
        message(FamilyMessage.InviteSent(normalizeEmail(email)))
        loadInvites()
    }

    fun cancelInvite(email: String) = busy {
        network { service.cancelInvite(email) }
        loadInvites()
    }

    fun syncNow() = busy {
        val result = network { service.sync() } ?: return@busy
        message(FamilyMessage.Synced(received = result.applied, sent = result.pushed))
    }

    fun signOut() = busy { service.signOut() }

    fun dismissMessage() = local.update { it.copy(message = null) }

    private suspend fun doJoin(keepLocalData: Boolean) {
        network { service.join(keepLocalData) } ?: return
        network { service.sync() }
    }

    private suspend fun loadInvites() {
        if (service.state.value !is FamilyState.Joined) return local.update { it.copy(invites = emptyList()) }
        val invites = runCatching { service.invites() }.getOrDefault(emptyList())
        local.update { it.copy(invites = invites.map { invite -> invite.email }.sorted()) }
    }

    /** Runs [action]; a network or server error becomes a message naming its cause (#124) and `null`. */
    private suspend fun <T> network(action: suspend () -> T): T? =
        runCatching { action() }.onFailure { e ->
            if (e is CancellationException) throw e
            message(FamilyMessage.failed(e))
        }.getOrNull()

    private fun message(m: FamilyMessage) = local.update { it.copy(message = m) }

    private fun busy(action: suspend () -> Unit) {
        local.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            try { action() } finally { local.update { it.copy(isBusy = false) } }
        }
    }

    private fun formatTime(epochMs: Long): String = formatDayTime(Instant.fromEpochMilliseconds(epochMs), clock.now(), timeZone)
}
