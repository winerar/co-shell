package app.coshell.greeting

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** What of the 1.0 greeting was already done: notification sent, pill closed with «×». Not part of the backup. */
interface GreetingStore {
    var notified: Boolean
    var dismissed: Boolean
}

@Singleton
class PrefsGreetingStore @Inject constructor(@ApplicationContext context: Context) : GreetingStore {
    private val prefs by lazy { context.getSharedPreferences("release_greeting", Context.MODE_PRIVATE) }

    override var notified: Boolean
        get() = prefs.getBoolean(NOTIFIED, false)
        set(value) = prefs.edit { putBoolean(NOTIFIED, value) }

    override var dismissed: Boolean
        get() = prefs.getBoolean(DISMISSED, false)
        set(value) = prefs.edit { putBoolean(DISMISSED, value) }

    private companion object {
        const val NOTIFIED = "notified"
        const val DISMISSED = "dismissed"
    }
}
