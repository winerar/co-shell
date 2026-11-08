package app.coshell.update.data

import java.io.File
import java.security.MessageDigest

/** Lowercase hex SHA-256 of a file, read in chunks (APKs are several megabytes). */
internal fun File.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { input ->
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
