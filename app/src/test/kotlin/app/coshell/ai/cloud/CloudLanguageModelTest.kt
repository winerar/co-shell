package app.coshell.ai.cloud

import app.coshell.core.data.remote.HttpClient
import app.coshell.core.data.remote.HttpRequest
import app.coshell.core.data.remote.HttpResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The cloud model (#635): token and body go to the Worker; every failure is a quiet `null`. */
class CloudLanguageModelTest {
    private class Http(val code: Int = 200, val body: String = """{"text":"[1]"}""", val fail: Boolean = false) : HttpClient {
        val requests = mutableListOf<HttpRequest>()
        override suspend fun send(request: HttpRequest): HttpResponse {
            requests += request
            if (fail) error("no network")
            return HttpResponse(code, body)
        }
    }

    private fun model(http: Http, url: String = "https://w.example", signedIn: Boolean = true, token: suspend () -> String = { "tok" }) =
        CloudLanguageModel(http, { token() }, url) { signedIn }

    @Test
    fun `sends system and prompt with the token and returns the text`() = runTest {
        val http = Http()
        assertEquals("[1]", model(http).complete("sys", "note \"x\""))
        val sent = http.requests.single()
        assertEquals("Bearer tok", sent.headers["Authorization"])
        assertTrue(sent.body!!.contains("\"system\":\"sys\""))
        assertTrue(sent.body!!.contains("note \\\"x\\\""))
    }

    @Test
    fun `not available without the address or without sign in, and then nothing is sent`() = runTest {
        val http = Http()
        assertFalse(model(http, url = "").available)
        assertFalse(model(http, signedIn = false).available)
        assertNull(model(http, signedIn = false).complete("s", "p"))
        assertTrue(http.requests.isEmpty())
    }

    @Test
    fun `server errors, network failure, bad json and token failure give null`() = runTest {
        assertNull(model(Http(code = 429)).complete("s", "p"))
        assertNull(model(Http(code = 502, body = "{}")).complete("s", "p"))
        assertNull(model(Http(fail = true)).complete("s", "p"))
        assertNull(model(Http(body = "not json")).complete("s", "p"))
        assertNull(model(Http(body = """{"text":"  "}""")).complete("s", "p"))
        assertNull(model(Http(), token = { error("expired") }).complete("s", "p"))
    }
}
