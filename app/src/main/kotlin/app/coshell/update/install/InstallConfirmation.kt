package app.coshell.update.install

import android.app.Activity
import android.content.ActivityNotFoundException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.coshell.update.log.UpdateJournal
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Opens the system install dialog from this activity whenever one is waiting and the activity is in
 * front. A start from a visible activity is allowed on every phone, unlike one from a receiver (#153);
 * a dialog requested while the app is in the background opens when the user comes back.
 */
fun <A> A.showInstallConfirmations(events: InstallEvents, journal: UpdateJournal) where A : Activity, A : LifecycleOwner {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            events.confirmation.value.filterNotNull().collect {
                val intent = events.confirmation.take() ?: return@collect
                try {
                    startActivity(intent)
                    journal.add("dialog started: ${intent.action ?: "session"}")
                } catch (e: ActivityNotFoundException) {
                    journal.add("dialog not started: ${e.message}")
                    events.emit(InstallOutcome.Failed(e.message))
                } catch (e: SecurityException) {
                    journal.add("dialog not started: ${e.message}")
                    events.emit(InstallOutcome.Failed(e.message))
                }
            }
        }
    }
}
