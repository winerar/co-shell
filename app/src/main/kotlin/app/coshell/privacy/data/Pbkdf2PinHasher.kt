package app.coshell.privacy.data

import android.util.Base64
import app.coshell.core.domain.privacy.PinHasher
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PIN → PBKDF2-HMAC-SHA256 with a random 16-byte salt (#145). A 4-digit PIN is guessable by brute force if someone has the
 * file, so the hash only keeps the digits off the disk; the real guard is the wait after wrong attempts and the phone's
 * own lock (the file is private to the app and not backed up).
 */
@Singleton
class Pbkdf2PinHasher @Inject constructor() : PinHasher {
    private val random = SecureRandom()

    override fun newSalt(): String = ByteArray(SALT_BYTES).also(random::nextBytes).let { Base64.encodeToString(it, Base64.NO_WRAP) }

    override fun hash(pin: String, salt: String): String {
        val spec = PBEKeySpec(pin.toCharArray(), Base64.decode(salt, Base64.NO_WRAP), ITERATIONS, KEY_BITS)
        return try {
            val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            Base64.encodeToString(key, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
        }
    }

    private companion object {
        const val SALT_BYTES = 16
        const val ITERATIONS = 120_000
        const val KEY_BITS = 256
    }
}
