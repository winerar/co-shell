package app.coshell.core.data.sync

import app.coshell.core.data.db.AccountEntity
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** #340: an older app editing a record of a newer one keeps the newer fields. */
class NewerFieldsSyncTest {
    private val usd = CurrencyCode("USD")
    private val server = FakeRemoteStore()
    private val old = Phone("member-old", server, startMs = 1_000_000)

    @AfterTest
    fun tearDown() = old.close()

    /** What a newer version would send: today's account plus fields this version does not know. */
    private suspend fun newerPhoneSaves(name: String, updatedAt: Long, extra: Map<String, JsonPrimitive>) {
        val entity = AccountEntity("acc", name, "USD", "CASH", 0, true, null, false, 0, updatedAt = updatedAt, updatedBy = "member-new")
        val base = Json.parseToJsonElement(SyncCodec.encode(entity).json).jsonObject
        val json = JsonObject(base + extra + ("v" to JsonPrimitive(3))).toString()
        server.push(listOf(SyncCodec.encode(entity).copy(json = json)))
    }

    private fun serverAccount(): JsonObject =
        Json.parseToJsonElement(server.records.single { it.record.id == "acc" }.record.json).jsonObject

    @Test
    fun `old phone edits a record of a newer one - newer fields survive`() = runTest {
        newerPhoneSaves("Cash", updatedAt = 1_000, extra = mapOf("position" to JsonPrimitive(5), "pin" to JsonPrimitive(true)))
        old.engine.sync()

        old.tick(); old.accounts.saveAccount(old.accounts.getAccount("acc")!!.copy(name = "Wallet"))
        old.engine.sync()

        val sent = serverAccount()
        assertEquals("Wallet", sent["name"]?.jsonPrimitive?.content, "own edit goes through")
        assertEquals("5", sent["position"]?.jsonPrimitive?.content, "the newer field is kept")
        assertEquals("true", sent["pin"]?.jsonPrimitive?.content)
        assertEquals(SyncPayload.SCHEMA, SyncPayload.schemaOf(sent.toString()), "stamped with this version's schema")
    }

    @Test
    fun `a newer version that dropped the value - nothing old comes back`() = runTest {
        newerPhoneSaves("Cash", updatedAt = 1_000, extra = mapOf("position" to JsonPrimitive(5)))
        old.engine.sync()
        newerPhoneSaves("Cash", updatedAt = 2_000, extra = emptyMap())
        old.engine.sync()

        old.tick(5_000); old.accounts.saveAccount(old.accounts.getAccount("acc")!!.copy(name = "Wallet"))
        old.engine.sync()

        assertNull(serverAccount()["position"], "extras are replaced by the winning version")
    }

    @Test
    fun `losing remote version does not replace saved fields`() = runTest {
        newerPhoneSaves("Cash", updatedAt = 1_000, extra = mapOf("position" to JsonPrimitive(5)))
        old.engine.sync()
        old.tick(10_000_000); old.accounts.saveAccount(old.accounts.getAccount("acc")!!.copy(name = "Wallet"))
        // A stale edit from the newer phone (older updatedAt) arrives before this phone pushes.
        newerPhoneSaves("Stale", updatedAt = 2_000, extra = mapOf("position" to JsonPrimitive(9)))
        old.engine.sync()

        assertEquals("Wallet", serverAccount()["name"]?.jsonPrimitive?.content)
        assertEquals("5", serverAccount()["position"]?.jsonPrimitive?.content)
    }

    @Test
    fun `records of this version have no extras and carry the schema`() = runTest {
        old.accounts.saveAccount(Account("acc", "Cash", usd))
        old.engine.sync()

        assertEquals(SyncPayload.SCHEMA, SyncPayload.schemaOf(serverAccount().toString()))
        assertEquals(emptyMap(), old.db.syncDao().extrasFor(server.records.map { it.record }))
    }

    @Test
    fun `joining another family forgets saved fields`() = runTest {
        newerPhoneSaves("Cash", updatedAt = 1_000, extra = mapOf("position" to JsonPrimitive(5)))
        old.engine.sync()
        old.db.syncDao().wipeSyncedData()

        assertEquals(emptyMap(), old.db.syncDao().extrasFor(server.records.map { it.record }))
    }
}
