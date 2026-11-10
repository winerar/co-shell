package app.coshell.core.data.remote

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerSentEventsTest {
    private suspend fun parse(vararg lines: String) = flowOf(*lines).serverSentEvents().toList()

    @Test
    fun `groups event and data lines, blank line ends an event`() = runTest {
        val events = parse(
            "event: put", """data: {"path":"/","data":null}""", "",
            "event: keep-alive", "data: null", "",
        )
        assertEquals(
            listOf(ServerSentEvent("put", """{"path":"/","data":null}"""), ServerSentEvent("keep-alive", "null")),
            events,
        )
    }

    @Test
    fun `joins multi-line data, skips comments and carriage returns`() = runTest {
        val events = parse(": hello", "event: patch\r", "data: a\r", "data:b", "\r")
        assertEquals(listOf(ServerSentEvent("patch", "a\nb")), events)
    }

    @Test
    fun `unfinished event at the end of the stream is dropped`() = runTest {
        assertEquals(emptyList(), parse("event: put", "data: x"))
    }
}
