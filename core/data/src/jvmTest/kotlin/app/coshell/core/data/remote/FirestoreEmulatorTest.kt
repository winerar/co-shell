package app.coshell.core.data.remote

import app.coshell.core.data.auth.AuthManager
import app.coshell.core.data.auth.AuthSession
import app.coshell.core.data.auth.FirebaseAuthApi
import app.coshell.core.data.auth.SessionStore
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.domain.access.CodeLookup
import app.coshell.core.domain.family.FamilyRemote
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Member
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * End-to-end: Auth emulator sign-in → family → invite → join → two phones sync over REST,
 * checked by the real security rules. Runs only inside `scripts/test-rules.sh --kotlin`
 * (the emulator sets FIRESTORE_EMULATOR_HOST); elsewhere it does nothing.
 */
class FirestoreEmulatorTest {
    private val firestoreHost = System.getenv("FIRESTORE_EMULATOR_HOST")
    private val authHost = System.getenv("FIREBASE_AUTH_EMULATOR_HOST")
    private val project = "demo-coshell"

    private inner class Phone(email: String, familyId: String = FamilyRemote.LEGACY_FAMILY_ID) {
        private val api = FirebaseAuthApi(
            JvmHttpClient, apiKey = "fake",
            identityHost = "http://$authHost/identitytoolkit.googleapis.com",
            tokenHost = "http://$authHost/securetoken.googleapis.com",
        )
        val auth = AuthManager(api, MemoryStore())
        private val client = FirestoreClient(JvmHttpClient, project, auth, host = "http://$firestoreHost")
        val family = FirestoreFamilyRemote(client)
        val db = createInMemoryCoShellDatabase()
        val transactions = RoomTransactionRepository(db.transactionDao(), WriteStamper { auth.uid })
        val engine = SyncEngine(db.syncDao(), FirestoreRemoteStore(client, familyId))
        lateinit var me: Member

        init {
            runBlocking {
                // The Auth emulator accepts an unsigned Google ID token given as JSON.
                val fakeGoogle = """{"sub":"${email.hashCode()}","email":"$email","email_verified":true,"name":"$email"}"""
                val s = auth.signInWithGoogle(URLEncoder.encode(fakeGoogle, "UTF-8"))
                me = Member(s.uid, s.name, s.email)
            }
        }
    }

    private class MemoryStore : SessionStore {
        var value: AuthSession? = null
        override fun load() = value
        override fun save(session: AuthSession?) { value = session }
    }

    private fun expense(id: String, minor: Long, note: String) = Transaction(
        id, TransactionType.EXPENSE, "acc", Money(minor, CurrencyCode("ARS")), Instant.parse("2026-11-09T10:00:00Z"), note = note,
    )

    @Test
    fun `family flow and two-phone sync against the emulator`() = runBlocking {
        if (firestoreHost == null || authHost == null) return@runBlocking
        clearEmulator()
        val owner = Phone("owner@gmail.com")
        val wife = Phone("Wife@Gmail.com")
        val stranger = Phone("stranger@gmail.com")
        val id = FamilyRemote.LEGACY_FAMILY_ID

        assertTrue(owner.family.create(id, owner.me))
        assertFalse(stranger.family.create(id, stranger.me), "the only family already exists")
        assertNull(stranger.family.family(id))
        assertNull(wife.family.inviteFor(wife.me.email))

        owner.family.invite(id, "wife@gmail.com", owner.me.uid)
        assertEquals(listOf("wife@gmail.com"), owner.family.invites(id).map { it.email })
        val invite = assertNotNull(wife.family.inviteFor(wife.me.email))
        wife.family.join(invite, wife.me)
        assertEquals(2, owner.family.family(id)!!.members.size)

        owner.transactions.saveTransaction(expense("t1", 1000_00, "кофе"))
        owner.engine.sync()
        wife.engine.sync()
        assertEquals("кофе", wife.transactions.getTransaction("t1")?.note)

        wife.transactions.saveTransaction(expense("t1", 1200_00, "кофе и булка"))
        wife.engine.sync()
        owner.engine.sync()
        assertEquals(Money(1200_00, CurrencyCode("ARS")), owner.transactions.getTransaction("t1")?.amount)

        val denied = assertFailsWith<ApiException> { stranger.engine.sync() }
        assertTrue(denied.isPermissionDenied)
    }

    @Test
    fun `many families, leaving, removal and deletion against the emulator`() = runBlocking {
        if (firestoreHost == null || authHost == null) return@runBlocking
        clearEmulator()
        val id = FamilyRemote.newFamilyId()
        val owner = Phone("owner@gmail.com", id)
        val wife = Phone("wife@gmail.com", id)
        val stranger = Phone("stranger@gmail.com")

        assertTrue(owner.family.create(id, owner.me))
        assertEquals(id, owner.family.pointer(owner.me.uid), "created together with the family")
        assertTrue(stranger.family.create(FamilyRemote.newFamilyId(), stranger.me), "everyone may start their own family")
        assertNull(stranger.family.family(id))

        // join: the pointer is written with it and the invitation is used up
        owner.family.invite(id, "wife@gmail.com", owner.me.uid)
        wife.family.join(assertNotNull(wife.family.inviteFor(wife.me.email)), wife.me)
        assertEquals(id, wife.family.pointer(wife.me.uid))
        assertNull(wife.family.inviteFor(wife.me.email))
        assertEquals(2, owner.family.family(id)!!.members.size)

        // leaving: the person and their pointer go
        wife.family.remove(id, wife.me.uid, wife.me.uid)
        assertNull(wife.family.pointer(wife.me.uid))
        assertNull(wife.family.family(id))
        assertEquals(listOf(owner.me.uid), owner.family.family(id)!!.members.map { it.uid })

        // removal by the owner: the removed person keeps the pointer — that is how their phone learns it
        owner.family.invite(id, "wife@gmail.com", owner.me.uid)
        wife.family.join(assertNotNull(wife.family.inviteFor(wife.me.email)), wife.me)
        owner.family.remove(id, wife.me.uid, owner.me.uid)
        assertEquals(id, wife.family.pointer(wife.me.uid))
        assertNull(wife.family.family(id))
        wife.family.clearPointer(wife.me.uid)
        assertNull(wife.family.pointer(wife.me.uid))

        // deleting the family: its records, invitations, the family itself and the owner's pointer
        owner.transactions.saveTransaction(expense("t1", 1000_00, "кофе"))
        owner.engine.sync()
        owner.family.invite(id, "late@gmail.com", owner.me.uid)
        owner.family.deleteFamily(id, owner.me.uid)
        assertNull(owner.family.family(id))
        assertNull(owner.family.pointer(owner.me.uid))
        assertNull(owner.family.inviteFor("late@gmail.com"))
    }

    @Test
    fun `invitation codes against the emulator`() = runBlocking {
        if (firestoreHost == null || authHost == null) return@runBlocking
        clearEmulator()
        val code = "K7QXM2PD9WTA"
        val documents = "http://$firestoreHost/v1/projects/$project/databases/(default)/documents"
        // The emulator treats "Bearer owner" as the admin (the robot's service account in production). HttpURLConnection has no PATCH: use commit.
        suspend fun seed(revoked: Boolean) {
            val fields = """{"label":{"stringValue":"Аня"},"revoked":{"booleanValue":$revoked},"maxDevices":{"integerValue":"2"},"devices":{"arrayValue":{}}}"""
            val body = """{"writes":[{"update":{"name":"projects/$project/databases/(default)/documents/access_codes/$code","fields":$fields}}]}"""
            val answer = JvmHttpClient.send(HttpRequest("POST", "$documents:commit", body, mapOf("Authorization" to "Bearer owner")))
            assertTrue(answer.isSuccess, answer.body)
        }
        val remote = FirestoreAccessRemote(JvmHttpClient, project, "fake", host = "http://$firestoreHost")

        seed(revoked = false)
        assertEquals(CodeLookup.Valid("Аня", emptyList(), 2), remote.lookup(code))
        assertEquals(CodeLookup.NotFound, remote.lookup("ABCDEFGHJKMN"))
        assertTrue(remote.addDevice(code, "phone-1"))
        assertTrue(remote.addDevice(code, "phone-2"))
        assertFalse(remote.addDevice(code, "phone-3"), "the limit is two phones")
        assertEquals(CodeLookup.Valid("Аня", listOf("phone-1", "phone-2"), 2), remote.lookup(code))

        seed(revoked = true)
        assertEquals(CodeLookup.Revoked("Аня"), remote.lookup(code))
        assertFalse(remote.addDevice(code, "phone-4"))
    }

    private suspend fun clearEmulator() {
        JvmHttpClient.send(HttpRequest("DELETE", "http://$firestoreHost/emulator/v1/projects/$project/databases/(default)/documents"))
    }
}
