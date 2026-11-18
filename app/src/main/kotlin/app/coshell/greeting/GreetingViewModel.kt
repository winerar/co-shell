package app.coshell.greeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.data.family.FamilyState
import app.coshell.core.domain.greeting.ReleaseGreeting
import app.coshell.core.domain.update.InstalledVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * The 1.0 greeting: the pill over «Обзор» until «×», and one system notification the first time it becomes due
 * (rule — [ReleaseGreeting]). Both are one-time: the flags live in [GreetingStore].
 */
@HiltViewModel
class GreetingViewModel @Inject constructor(
    family: GreetingFamily,
    installed: InstalledVersion,
    private val store: GreetingStore,
    private val notifier: GreetingNotices,
) : ViewModel() {

    private val due = family.state().map { state ->
        val joined = state as? FamilyState.Joined
        ReleaseGreeting.isDue(
            versionName = installed.versionName,
            isMember = joined != null,
            isOwner = joined != null && joined.family.ownerUid == joined.me.uid,
        )
    }.distinctUntilChanged()

    private val dismissed = MutableStateFlow(store.dismissed)

    /** The pill is shown while the greeting is due and was not closed. */
    val pillVisible: StateFlow<Boolean> = combine(due, dismissed) { isDue, closed -> isDue && !closed }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        due.onEach { isDue ->
            if (isDue && !store.notified) {
                store.notified = true // one attempt: no permission → the pill alone, never a late notification
                notifier.show()
            }
        }.launchIn(viewModelScope)
    }

    fun dismiss() {
        store.dismissed = true
        dismissed.value = true
    }
}
