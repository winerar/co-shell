package app.coshell.ai

import android.content.Context
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

/** The one model of the add-on: Gemma 4 E2B (Apache 2.0, public, no login), chosen by the on-device benchmark (docs/research/on-device-ai.md). */
class ModelFile(context: Context) {
    val file = File(context.filesDir, "models/gemma-4-E2B-it.litertlm")
    private val part = File(file.path + ".part")

    fun state(): Int = when {
        file.isFile -> AddonProtocol.MODEL_READY
        part.isFile && part.length() > 0 -> AddonProtocol.MODEL_PARTIAL
        else -> AddonProtocol.MODEL_MISSING
    }

    fun delete() {
        file.delete()
        part.delete()
    }

    /** Resumable download: the `.part` file grows, then is renamed; a dropped connection continues where it stopped. */
    fun download(onProgress: (done: Long, total: Long) -> Unit, cancelled: () -> Boolean) {
        part.parentFile?.mkdirs()
        var have = if (part.exists()) part.length() else 0L
        val conn = (URL(URL_OF_MODEL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            if (have > 0) setRequestProperty("Range", "bytes=$have-")
        }
        try {
            val code = conn.responseCode
            check(code == 200 || code == 206) { "HTTP $code" }
            if (code == 200) have = 0 // the server ignored Range: start over
            val total = have + conn.contentLengthLong
            RandomAccessFile(part, "rw").use { out ->
                out.seek(have)
                if (have == 0L) out.setLength(0)
                conn.inputStream.use { input ->
                    val buf = ByteArray(256 * 1024)
                    var done = have
                    var lastReport = 0L
                    while (true) {
                        if (cancelled()) return
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        done += n
                        val now = System.currentTimeMillis()
                        if (now - lastReport > 400) { lastReport = now; onProgress(done, total) }
                    }
                    onProgress(done, total)
                }
            }
            check(part.renameTo(file)) { "cannot rename ${part.name}" }
        } finally {
            conn.disconnect()
        }
    }

    private companion object {
        const val URL_OF_MODEL = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    }
}
