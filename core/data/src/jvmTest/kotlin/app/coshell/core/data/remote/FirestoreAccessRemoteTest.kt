package app.coshell.core.data.remote

import app.coshell.core.domain.access.CodeLookup
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FirestoreAccessRemoteTest {
    private val sent = mutableListOf<HttpRequest>()
    private var answer = HttpResponse(200, "{}")
    private val remote = FirestoreAccessRemote({ sent += it; answer }, "demo", "KEY")

    private fun document(revoked: Boolean = false, devices: List<String> = emptyList()) = """
        {"name":"projects/demo/databases/(default)/documents/access_codes/K7QXM2PD9WTA","fields":{
          "label":{"stringValue":"Аня"},"revoked":{"booleanValue":$revoked},"maxDevices":{"integerValue":"3"},
          "devices":{"arrayValue":{"values":[${devices.joinToString(",") { """{"stringValue":"$it"}""" }}]}}}}
    """.trimIndent()

    @Test
    fun `reads a code with the public key and no sign-in`() = runTest {
        answer = HttpResponse(200, document(devices = listOf("a", "b")))
        assertEquals(CodeLookup.Valid("Аня", listOf("a", "b"), 3), remote.lookup("K7QXM2PD9WTA"))
        val request = sent.single()
        assertEquals("GET", request.method)
        assertEquals("https://firestore.googleapis.com/v1/projects/demo/databases/(default)/documents/access_codes/K7QXM2PD9WTA?key=KEY", request.url)
        assertTrue(request.headers.isEmpty(), "no Authorization header")
    }

    @Test
    fun `a revoked code is revoked and a code without devices has none`() = runTest {
        answer = HttpResponse(200, document(revoked = true))
        assertEquals(CodeLookup.Revoked("Аня"), remote.lookup("K7QXM2PD9WTA"))
        answer = HttpResponse(200, """{"fields":{"label":{"stringValue":"Паша"},"maxDevices":{"integerValue":"2"},"devices":{"arrayValue":{}}}}""")
        assertEquals(CodeLookup.Valid("Паша", emptyList(), 2), remote.lookup("K7QXM2PD9WTA"))
    }

    @Test
    fun `a missing code and a hidden one both look like not found`() = runTest {
        answer = HttpResponse(404, """{"error":{"status":"NOT_FOUND"}}""")
        assertEquals(CodeLookup.NotFound, remote.lookup("ABCDEFGHJKMN"))
        answer = HttpResponse(403, """{"error":{"status":"PERMISSION_DENIED"}}""")
        assertEquals(CodeLookup.NotFound, remote.lookup("ABCDEFGHJKMN"))
    }

    @Test
    fun `a server error is an error and not a verdict`() = runTest {
        answer = HttpResponse(503, """{"error":{"status":"UNAVAILABLE"}}""")
        assertFailsWith<ApiException> { remote.lookup("K7QXM2PD9WTA") }
    }

    @Test
    fun `adds a device with one atomic append and tells when it is refused`() = runTest {
        assertTrue(remote.addDevice("K7QXM2PD9WTA", "phone-1"))
        val request = sent.single()
        assertEquals("POST", request.method)
        assertTrue(request.url.endsWith(":commit?key=KEY"))
        assertTrue("appendMissingElements" in request.body!! && "phone-1" in request.body!! && "\"devices\"" in request.body!!)

        answer = HttpResponse(403, """{"error":{"status":"PERMISSION_DENIED"}}""")
        assertFalse(remote.addDevice("K7QXM2PD9WTA", "phone-2"))
        answer = HttpResponse(500, "{}")
        assertFailsWith<ApiException> { remote.addDevice("K7QXM2PD9WTA", "phone-3") }
    }
}
