package app.coshell.language

import java.io.File

/**
 * Texts that exist only in Russian on purpose (translations are paused until publication, owner 2026-11-24).
 * The list lives in `src/test/resources/translations-pending.txt`; [TranslationsTest] checks it is exact.
 */
object TranslationPending {
    val keys: Set<String> by lazy {
        File("src/test/resources/translations-pending.txt").readLines()
            .map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.toSet()
    }
}
