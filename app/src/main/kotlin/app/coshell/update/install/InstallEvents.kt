package app.coshell.update.install

import android.content.Intent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Result of an install that did not end with the app restarting. */
sealed interface InstallOutcome {
    /** The user closed the system confirmation dialog. */
    data object Cancelled : InstallOutcome
    data class Failed(val message: String?) : InstallOutcome
}

/**
 * Carries install events from [InstallResultReceiver]: results to the update screen, and the system
 * confirmation dialog to the visible activity ([showInstallConfirmations]). The receiver must not open
 * the dialog itself: Xiaomi (MIUI/HyperOS) silently blocks activity starts from a receiver (#153).
 */
@Singleton
class InstallEvents @Inject constructor() {
    private val events = MutableSharedFlow<InstallOutcome>(extraBufferCapacity = 1)
    val outcomes: SharedFlow<InstallOutcome> = events.asSharedFlow()

    /** The system install dialog waiting for a visible activity to open it. */
    val confirmation = OneShotSlot<Intent>()

    fun emit(outcome: InstallOutcome) {
        events.tryEmit(outcome)
    }
}
