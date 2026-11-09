package app.coshell.core.data.remote

import java.net.HttpURLConnection
import java.net.URI

/** Test HTTP client (same approach as the app's UrlConnectionHttpClient). */
object JvmHttpClient : HttpClient {
    override suspend fun send(request: HttpRequest): HttpResponse {
        val c = URI(request.url).toURL().openConnection() as HttpURLConnection
        try {
            c.requestMethod = request.method
            request.headers.forEach { (k, v) -> c.setRequestProperty(k, v) }
            if (request.body != null) {
                c.doOutput = true
                c.setRequestProperty("Content-Type", request.contentType)
                c.outputStream.use { it.write(request.body.toByteArray()) }
            }
            val code = c.responseCode
            val body = (if (code in 200..299) c.inputStream else c.errorStream)?.use { it.readBytes().decodeToString() }.orEmpty()
            return HttpResponse(code, body)
        } finally {
            c.disconnect()
        }
    }
}
