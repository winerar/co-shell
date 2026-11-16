package app.coshell.core.data.sync

import app.coshell.core.data.db.AccountEntity
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** #334: a record of a newer sync schema closes entry on this phone until the update. */
class VersionGateSyncTest {
    private val server = FakeRemoteStore()
    private val phone = Phone("member-old", server, startMs = 1_000_000)

    @AfterTest
    fun tearDown() = phone.close()

    private suspend fun otherPhoneSaves(id: String, schema: Int?) {
        val entity = AccountEntity(id, "Cash", "USD", "CASH", 0, true, null, false, 0, updatedAt = 1_000, updatedBy = "member-new")
        val base = Json.parseToJsonElement(SyncCodec.encode(entity).json).jsonObject - SyncPayload.KEY
        val json = JsonObject(if (schema == null) base else base + (SyncPayload.KEY to JsonPrimitive(schema))).toString()
        server.push(listOf(SyncCodec.encode(entity).copy(json = json)))
    }

    private suspend fun locked() = VersionGate.isLocked(phone.db.syncDao().state(VersionGate.KEY))

    @Test
    fun `a record of a newer schema closes entry`() = runTest {
        otherPhoneSaves("acc", SyncPayload.SCHEMA + 1)
        phone.engine.sync()

        assertTrue(locked())
        assertEquals("Cash", phone.accounts.getAccount("acc")?.name, "the data is still shown")
    }

    @Test
    fun `records of this or an older schema keep entry open`() = runTest {
        otherPhoneSaves("a", SyncPayload.SCHEMA)
        otherPhoneSaves("b", null)
        phone.accounts.saveAccount(Account("own", "Mine", CurrencyCode("USD")))
        phone.engine.sync()

        assertFalse(locked())
    }

    @Test
    fun `the gate stays closed after later pulls of older records`() = runTest {
        otherPhoneSaves("a", SyncPayload.SCHEMA + 1)
        phone.engine.sync()
        otherPhoneSaves("b", SyncPayload.SCHEMA)
        phone.engine.sync()

        assertTrue(locked(), "only an update opens it")
    }

    @Test
    fun `joining another family forgets the gate`() = runTest {
        otherPhoneSaves("a", SyncPayload.SCHEMA + 1)
        phone.engine.sync()
        phone.db.syncDao().wipeSyncedData()

        assertNull(phone.db.syncDao().state(VersionGate.KEY))
    }

    @Test
    fun `the stored schema opens once this version is as new`() {
        assertFalse(VersionGate.isLocked(null))
        assertFalse(VersionGate.isLocked("${SyncPayload.SCHEMA}"))
        assertTrue(VersionGate.isLocked("${SyncPayload.SCHEMA + 1}"))
        assertNull(VersionGate.raised("5", 4), "never lowered")
        assertNull(VersionGate.raised("5", null))
        assertEquals("6", VersionGate.raised("5", 6))
    }
}
