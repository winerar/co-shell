package app.coshell.core.data.sync

import app.coshell.core.data.family.FakeAuth
import app.coshell.core.data.remote.ApiException
import app.coshell.core.data.remote.EventStreamClient
import app.coshell.core.data.remote.HttpRequest
import app.coshell.core.data.remote.HttpResponse
import app.coshell.core.data.remote.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FamilyBellTest {
    private val url = "https://demo-default-rtdb.firebaseio.com"
    private val auth = FakeAuth(FakeAuth.session("owner"))

    /** Realtime Database with the same member rule as `firebase/database.rules.json`. */
    private class FakeRtdb {
        val members = mutableSetOf<String>()
        val requests = mutableListOf<HttpRequest>()
        fun send(r: HttpRequest, uid: String): HttpResponse {
            requests += r
            val path = r.url.substringAfter("/families/main/").substringBefore(".json")
            val allowed = uid in members || (path == "members/$uid" && r.method == "PUT" && members.isEmpty())
            if (!allowed) return HttpResponse(401, """{"error":"Permission denied"}""")
            if (path == "members/$uid") members += uid
            if (path == "members") members += Regex("\"([^\"]+)\":true").findAll(r.body!!).map { it.groupValues[1] }
            return HttpResponse(200, "null")
        }
    }

    private val rtdb = FakeRtdb()
    private fun bell(stream: EventStreamClient = EventStreamClient { flow {} }, dbUrl: String = url) =
        FamilyBell({ rtdb.send(it, auth.uid!!) }, stream, dbUrl, auth)

    private fun ping(by: String) = listOf("event: put", """data: {"path":"/","data":{"at":1,"by":"$by"}}""", "")

    @Test
    fun `ring writes server time and author with the ID token`() = runTest {
        rtdb.members += "owner"
        bell().ring()
        val r = rtdb.requests.single()
        assertEquals("PUT", r.method)
        assertEquals("$url/families/main/ping.json?auth=token-owner", r.url)
        assertEquals("""{"at":{".sv":"timestamp"},"by":"owner"}""", r.body)
    }

    @Test
    fun `refused ring throws so the caller can ignore it`() = runTest {
        val failed = runCatching { bell().ring() }.exceptionOrNull()
        assertTrue(failed is ApiException && failed.code == 401)
    }

    @Test
    fun `first member registers themselves, then adds the others`() = runTest {
        assertTrue(bell().register(setOf("owner", "wife")))
        assertEquals(setOf("owner", "wife"), rtdb.members)
    }

    @Test
    fun `a newcomer cannot register into a taken list`() = runTest {
        rtdb.members += "owner"
        val stranger = FakeAuth(FakeAuth.session("stranger"))
        val bell = FamilyBell({ rtdb.send(it, "stranger") }, EventStreamClient { flow {} }, url, stranger)
        assertFalse(bell.register(setOf("stranger")))
        assertEquals(setOf("owner"), rtdb.members)
    }

    @Test
    fun `without database url the bell is silent`() = runTest {
        bell(dbUrl = "").ring()
        assertFalse(bell(dbUrl = "").register(setOf("owner")))
        assertEquals(emptyList(), bell(dbUrl = "").rings(setOf("owner")).toList())
        assertTrue(rtdb.requests.isEmpty())
    }

    @Test
    fun `rings from others pass, own rings, empty node and keep-alive do not`() = runTest {
        val stream = EventStreamClient { _ ->
            lines(listOf("event: put", """data: {"path":"/","data":null}""", "") + ping("owner") +
                listOf("event: keep-alive", "data: null", "") + ping("wife"))
        }
        assertEquals(listOf(Unit), bell(stream).rings(setOf("owner", "wife")).take(1).toList())
    }

    @Test
    fun `reconnects with a fresh token after auth_revoked and after errors`() = runTest {
        var connects = 0
        val stream = EventStreamClient { requested ->
            connects++
            assertTrue(requested.endsWith("/families/main/ping.json?auth=token-owner"))
            when (connects) {
                1 -> lines(listOf("event: auth_revoked", "data: credential is no longer valid", "") + ping("wife"))
                2 -> flow { throw ApiException(503, null, "down") }
                else -> lines(ping("wife"))
            }
        }
        val errors = mutableListOf<Throwable>()
        assertEquals(listOf(Unit), bell(stream).rings(setOf("owner"), onError = { errors += it }).take(1).toList())
        assertEquals(3, connects)
        assertEquals(1, errors.size)
    }

    @Test
    fun `ring author is read from root and from by path`() {
        assertFalse(ServerSentEvent("put", """{"path":"/by","data":"me"}""").isRingFrom("me"))
        assertTrue(ServerSentEvent("put", """{"path":"/by","data":"wife"}""").isRingFrom("me"))
        assertTrue(ServerSentEvent("patch", """{"path":"/at","data":5}""").isRingFrom("me"))
        assertFalse(ServerSentEvent("keep-alive", "null").isRingFrom("me"))
    }

    private fun lines(list: List<String>): Flow<String> = flow { list.forEach { emit(it) } }
}
