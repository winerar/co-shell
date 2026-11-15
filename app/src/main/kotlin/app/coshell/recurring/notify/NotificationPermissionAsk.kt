package app.coshell.recurring.notify

import android.content.Context
import android.os.Build
import app.coshell.core.domain.templates.TemplateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Android 13+ asks before an app may notify. We ask once, when the first template gets «Повтор» (#143):
 * that is when a notification starts to mean something. Declined — the system dialog is not repeated;
 * the reminders can be turned on in the phone's settings.
 */
class NotificationPermissionAsk @Inject constructor(
    @ApplicationContext private val context: Context,
    private val templates: TemplateRepository,
) {
    private val prefs = context.getSharedPreferences("recurring_reminders", Context.MODE_PRIVATE)

    /** Suspends until the question is due; returns `false` when it never will be on this phone. */
    suspend fun awaitDue(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        if (PaymentNotices.canNotify(context) || prefs.getBoolean(ASKED, false)) return false
        templates.observeTemplates().first { list -> list.any { it.schedule != null } }
        prefs.edit().putBoolean(ASKED, true).apply()
        return !PaymentNotices.canNotify(context)
    }

    private companion object {
        const val ASKED = "permission_asked"
    }
}
