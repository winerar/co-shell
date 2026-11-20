package app.coshell.privacy.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.privacy.PinAttempts
import app.coshell.core.domain.privacy.PrivacyOptions
import app.coshell.core.domain.privacy.PrivacySettings
import app.coshell.core.domain.privacy.RelockDelay
import app.coshell.core.domain.privacy.StoredPin
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/**
 * [PrivacySettings] in its own SharedPreferences file (#145): a device preference — not synced, not in the backup,
 * and the app has `allowBackup="false"`, so the PIN hash never leaves the phone.
 */
@Singleton
class PrefsPrivacySettings @Inject constructor(@ApplicationContext context: Context) : PrivacySettings {

    private val prefs by lazy { context.getSharedPreferences("privacy", Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<PrivacyOptions?>(null)

    override val options: Flow<PrivacyOptions> = state
        .onStart { if (state.value == null) state.compareAndSet(null, io { read() }) }
        .filterNotNull()

    override suspend fun update(change: (PrivacyOptions) -> PrivacyOptions) {
        val current = state.value ?: io { read() }
        val next = change(current)
        state.value = next
        io { write(next) }
    }

    override suspend fun pin(): StoredPin? = io {
        val salt = prefs.getString(KEY_SALT, null)
        val hash = prefs.getString(KEY_HASH, null)
        if (salt != null && hash != null) StoredPin(salt, hash) else null
    }

    override suspend fun setPin(pin: StoredPin?) {
        io {
            prefs.edit {
                if (pin == null) remove(KEY_SALT).remove(KEY_HASH) else putString(KEY_SALT, pin.salt).putString(KEY_HASH, pin.hash)
                remove(KEY_FAILED).remove(KEY_BLOCKED)
            }
        }
        update { it.copy(pinEnabled = pin != null, biometric = pin != null && it.biometric) }
    }

    override suspend fun attempts(): PinAttempts = io { PinAttempts(prefs.getInt(KEY_FAILED, 0), prefs.getLong(KEY_BLOCKED, 0)) }

    override suspend fun setAttempts(attempts: PinAttempts) {
        io { prefs.edit { putInt(KEY_FAILED, attempts.failed).putLong(KEY_BLOCKED, attempts.blockedUntil) } }
    }

    private fun read() = PrivacyOptions(
        pinEnabled = prefs.getBoolean(KEY_PIN, false) && prefs.contains(KEY_HASH),
        biometric = prefs.getBoolean(KEY_BIOMETRIC, false),
        relock = RelockDelay.parse(prefs.getString(KEY_RELOCK, null)),
        hideOnLaunch = prefs.getBoolean(KEY_HIDE_LAUNCH, false),
        hideFaceDown = prefs.getBoolean(KEY_FACE_DOWN, true),
        hideInRecents = prefs.getBoolean(KEY_RECENTS, false),
        blockScreenshots = prefs.getBoolean(KEY_SCREENSHOTS, false),
    )

    private fun write(o: PrivacyOptions) = prefs.edit {
        putBoolean(KEY_PIN, o.pinEnabled)
        putBoolean(KEY_BIOMETRIC, o.biometric)
        putString(KEY_RELOCK, o.relock.name)
        putBoolean(KEY_HIDE_LAUNCH, o.hideOnLaunch)
        putBoolean(KEY_FACE_DOWN, o.hideFaceDown)
        putBoolean(KEY_RECENTS, o.hideInRecents)
        putBoolean(KEY_SCREENSHOTS, o.blockScreenshots)
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val KEY_PIN = "pin_enabled"
        const val KEY_SALT = "pin_salt"
        const val KEY_HASH = "pin_hash"
        const val KEY_FAILED = "pin_failed"
        const val KEY_BLOCKED = "pin_blocked_until"
        const val KEY_BIOMETRIC = "biometric"
        const val KEY_RELOCK = "relock"
        const val KEY_HIDE_LAUNCH = "hide_on_launch"
        const val KEY_FACE_DOWN = "hide_face_down"
        const val KEY_RECENTS = "hide_in_recents"
        const val KEY_SCREENSHOTS = "block_screenshots"
    }
}

