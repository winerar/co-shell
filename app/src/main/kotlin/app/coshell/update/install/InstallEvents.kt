package app.coshell.update.install

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

/** Carries install results from [InstallResultReceiver] to the update screen. */
@Singleton
class InstallEvents @Inject constructor() {
    private val events = MutableSharedFlow<InstallOutcome>(extraBufferCapacity = 1)
    val outcomes: SharedFlow<InstallOutcome> = events.asSharedFlow()

    fun emit(outcome: InstallOutcome) {
        events.tryEmit(outcome)
    }
}
