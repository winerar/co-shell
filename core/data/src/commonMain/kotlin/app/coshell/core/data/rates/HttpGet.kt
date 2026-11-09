package app.coshell.core.data.rates

/** Minimal HTTP GET used by rates sources; the Android implementation lives in `app` (no SDKs). */
fun interface HttpGet {
    /** Body of a `200 OK` answer; throws on any other status or network error. */
    suspend fun get(url: String): String
}
