package app.coshell.core.data.remote

/** Minimal HTTP for REST APIs (Firestore, Firebase Auth). The Android implementation lives in `app`. */
fun interface HttpClient {
    /** Never throws for HTTP error codes — only for network failures. */
    suspend fun send(request: HttpRequest): HttpResponse
}

data class HttpRequest(
    val method: String,
    val url: String,
    val body: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val contentType: String = "application/json; charset=utf-8",
)

data class HttpResponse(val code: Int, val body: String) {
    val isSuccess get() = code in 200..299
}

/** HTTP error from a Google API; [status] is the `error.status` text, e.g. `PERMISSION_DENIED`. */
class ApiException(val code: Int, val status: String?, message: String) : Exception("HTTP $code $status: $message") {
    val isPermissionDenied get() = code == 403 || status == "PERMISSION_DENIED"
    val isNotFound get() = code == 404 || status == "NOT_FOUND"
    val isAlreadyExists get() = code == 409 || status == "ALREADY_EXISTS" || status == "FAILED_PRECONDITION"
    val isUnauthenticated get() = code == 401 || status == "UNAUTHENTICATED"
}
