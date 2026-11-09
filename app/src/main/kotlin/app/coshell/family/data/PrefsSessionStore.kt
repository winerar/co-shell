package app.coshell.family.data

import android.content.Context
import app.coshell.core.data.auth.AuthSession
import app.coshell.core.data.auth.SessionStore
import kotlinx.serialization.json.Json

/**
 * Firebase session in app-private SharedPreferences: not readable by other apps, and Android
 * cloud backup is off (`allowBackup="false"`), so tokens never leave the phone.
 */
class PrefsSessionStore(context: Context) : SessionStore {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    override fun load(): AuthSession? =
        prefs.getString(KEY, null)?.let { runCatching { Json.decodeFromString<AuthSession>(it) }.getOrNull() }

    override fun save(session: AuthSession?) {
        prefs.edit().apply {
            if (session == null) remove(KEY) else putString(KEY, Json.encodeToString(session))
        }.apply()
    }

    companion object {
        const val FILE = "auth"
        private const val KEY = "session"
    }
}
