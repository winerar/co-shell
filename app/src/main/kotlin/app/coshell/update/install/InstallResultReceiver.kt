package app.coshell.update.install

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.core.content.IntentCompat
import app.coshell.update.log.UpdateJournal
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receives [PackageInstaller] session status: passes the system confirmation dialog to the visible activity,
 * reports cancellation and errors. On success Android only kills the old process; it does not reopen the app,
 * and since Android 14 the installer callback may not start an activity from the background (#299).
 */
@AndroidEntryPoint
class InstallResultReceiver : BroadcastReceiver() {

    @Inject lateinit var events: InstallEvents
    @Inject lateinit var journal: UpdateJournal

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        journal.add("status $status ${intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE).orEmpty()}".trimEnd())
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                // The visible activity opens it (InstallEvents): a start from here is blocked on Xiaomi (#153).
                if (confirm == null) events.emit(InstallOutcome.Failed(null)) else events.confirmation.put(confirm)
            }
            PackageInstaller.STATUS_SUCCESS -> Unit
            PackageInstaller.STATUS_FAILURE_ABORTED -> events.emit(InstallOutcome.Cancelled)
            else -> events.emit(InstallOutcome.Failed(intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)))
        }
    }
}
