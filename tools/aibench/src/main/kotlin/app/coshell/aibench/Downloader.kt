package app.coshell.aibench

import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

/** Resumable download to [dest] (a `.part` file grows, then is renamed): a dropped connection continues where it stopped. */
object Downloader {
    fun download(url: String, dest: File, token: String, onProgress: (done: Long, total: Long) -> Unit, cancelled: () -> Boolean) {
        val part = File(dest.path + ".part")
        part.parentFile?.mkdirs()
        var have = if (part.exists()) part.length() else 0L
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            if (have > 0) setRequestProperty("Range", "bytes=$have-")
            if (token.isNotBlank()) setRequestProperty("Authorization", "Bearer ${token.trim()}")
        }
        try {
            val code = conn.responseCode
            check(code == 200 || code == 206) { if (code == 401 || code == 403) "HTTP $code: модель закрыта — нужен токен Hugging Face и принятые условия на странице модели" else "HTTP $code" }
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
            check(part.renameTo(dest)) { "cannot rename ${part.name}" }
        } finally {
            conn.disconnect()
        }
    }
}
