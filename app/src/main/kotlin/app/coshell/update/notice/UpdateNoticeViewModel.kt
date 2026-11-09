package app.coshell.update.notice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.update.AppRelease
import app.coshell.core.domain.update.WhatsNewUseCase
import app.coshell.update.UpdateController
import app.coshell.update.UpdatePhase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * App-wide update notices: the quiet pill over the bottom bar when a newer build appears (on start or
 * while the app is open), the dot on «Ещё», and «Что нового» once after an update.
 */
@HiltViewModel
class UpdateNoticeViewModel @Inject constructor(
    private val controller: UpdateController,
    whatsNew: WhatsNewUseCase,
) : ViewModel() {

    private val _whatsNew = MutableStateFlow<AppRelease?>(null)
    val whatsNew: StateFlow<AppRelease?> = _whatsNew.asStateFlow()

    /** Version hidden with «×»: the pill comes back with a newer one or on the next launch. */
    private val dismissedCode = MutableStateFlow(0)

    /** A newer release, for the «Ещё» dot and the settings row; null when up to date. */
    val available: StateFlow<AppRelease?> = controller.available

    /** Release for the pill; hidden while it is dismissed or already downloading/installing. */
    val pill: StateFlow<AppRelease?> =
        combine(controller.available, dismissedCode, controller.phase) { release, dismissed, phase ->
            release?.takeIf {
                it.versionCode > dismissed && phase !is UpdatePhase.Downloading && phase !is UpdatePhase.Installing
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var watcher: Job? = null

    init {
        viewModelScope.launch { _whatsNew.value = whatsNew() }
    }

    /** While the app is in the foreground: check now and again every [RECHECK] (the use case limits network calls). */
    fun startWatching() {
        watcher?.cancel()
        watcher = viewModelScope.launch {
            while (isActive) {
                controller.autoCheck()
                delay(RECHECK)
            }
        }
    }

    fun stopWatching() {
        watcher?.cancel()
        watcher = null
    }

    /** Tap on the pill: the download starts right away; the caller opens the update screen. */
    fun startUpdate(release: AppRelease) = controller.download(release)

    fun dismissPill(release: AppRelease) {
        dismissedCode.value = release.versionCode
    }

    fun dismissWhatsNew() {
        _whatsNew.value = null
    }

    companion object {
        val RECHECK = 15.minutes
    }
}
