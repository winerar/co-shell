package app.coshell.core.domain.privacy

import kotlinx.coroutines.flow.Flow

/** «Блокировать после выхода» (#145): how long the app may stay in the background before it asks for the PIN again. */
enum class RelockDelay(val millis: Long) {
    NOW(0),
    ONE_MINUTE(60_000),
    FIVE_MINUTES(300_000),
    ;

    companion object {
        /** Saved as [name]; unknown or missing → [ONE_MINUTE] (time to check a bank app and come back). */
        fun parse(stored: String?): RelockDelay = entries.firstOrNull { it.name == stored } ?: ONE_MINUTE
    }
}

/**
 * «Ещё → Конфиденциальность» (#145, board `Priv145Settings`). Everything is off until the person turns the PIN on:
 * a newcomer opens the app and uses it (owner.md). Kept on the device only: not synced, not in the backup.
 */
data class PrivacyOptions(
    /** A PIN is set and asked for on entry. */
    val pinEnabled: Boolean = false,
    /** Fingerprint or face instead of the PIN, when the phone can. */
    val biometric: Boolean = false,
    val relock: RelockDelay = RelockDelay.ONE_MINUTE,
    /** The app always opens with the sums hidden. */
    val hideOnLaunch: Boolean = false,
    /** Laying the phone screen down hides the sums (#324). */
    val hideFaceDown: Boolean = true,
    /** A blank card instead of the screen in the system «recent apps». */
    val hideInRecents: Boolean = false,
    /** No screenshots and no screen recording. */
    val blockScreenshots: Boolean = false,
) {
    /** The recents preview is hidden whenever screenshots are blocked too (the system does both with one flag). */
    val previewHidden: Boolean get() = hideInRecents || blockScreenshots
}

/** A saved PIN: never the digits — a salted slow hash ([PinHasher]). */
data class StoredPin(val salt: String, val hash: String)

/** Device storage of [PrivacyOptions], the PIN and the failed attempts. */
interface PrivacySettings {
    val options: Flow<PrivacyOptions>
    suspend fun update(change: (PrivacyOptions) -> PrivacyOptions)

    suspend fun pin(): StoredPin?
    /** Saves a new PIN and turns the lock on; `null` removes it and turns the lock (and biometrics) off. */
    suspend fun setPin(pin: StoredPin?)

    suspend fun attempts(): PinAttempts
    suspend fun setAttempts(attempts: PinAttempts)
}

/** Salted slow hash of the PIN; the app implements it (PBKDF2 needs the platform). */
interface PinHasher {
    fun newSalt(): String
    fun hash(pin: String, salt: String): String
}
