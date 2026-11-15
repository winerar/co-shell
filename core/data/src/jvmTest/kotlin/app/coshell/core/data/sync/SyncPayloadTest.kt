package app.coshell.core.data.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SyncPayloadTest {
    @Test
    fun `payload without a number is schema 1`() {
        assertEquals(1, SyncPayload.schemaOf("""{"id":"a","name":"Cash"}"""))
        assertEquals(1, SyncPayload.schemaOf("not json"))
        assertEquals(3, SyncPayload.schemaOf("""{"id":"a","v":3}"""))
    }

    @Test
    fun `own fields win over saved ones, unknown ones stay`() {
        val merged = SyncPayload.withExtras("""{"name":"Wallet"}""", """{"name":"Old","sortOrder":5,"v":7}""")
        assertEquals("""{"name":"Wallet","sortOrder":5,"v":${SyncPayload.SCHEMA}}""", merged)
    }

    @Test
    fun `unknown keys ignore the schema number`() {
        assertEquals("""{"pin":true}""", SyncPayload.unknownKeys("""{"name":"a","pin":true,"v":3}""", setOf("name")))
        assertNull(SyncPayload.unknownKeys("""{"name":"a","v":3}""", setOf("name")))
    }
}
