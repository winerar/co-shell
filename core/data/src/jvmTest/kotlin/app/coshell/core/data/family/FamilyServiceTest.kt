package app.coshell.core.data.family

import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.sync.FakeRemoteStore
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class FamilyServiceTest {
    private val records = FakeRemoteStore()
    private val familyServer = FakeFamilyServer()
    private val phones = mutableListOf<TestPhone>()

    inner class TestPhone(session: String? = null, configured: Boolean = true) {
        val auth = FakeAuth(session?.let { FakeAuth.session(it) })
        val db = createInMemoryCoShellDatabase()
        private val stamper = WriteStamper { auth.uid }
        val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)
        val backups = mutableListOf<String>()
        var now = Instant.fromEpochMilliseconds(1_700_000_000_000)
        private val clock = object : Clock { override fun now() = this@TestPhone.now }
        fun service(configured: Boolean = true) = FamilyService(
            configured, auth, FakeFamilyRemote(familyServer, auth), db.syncDao(),
            SyncEngine(db.syncDao(), records), JsonBackupRepository(db, stamper), clock,
        ) { backups += it }
        val family = service(configured)
        init { phones += this }
    }

    @AfterTest
    fun tearDown() = phones.forEach { it.db.close() }

    private val usd = CurrencyCode("USD")

    @Test
    fun `build without keys stays local`() = runTest {
        val phone = TestPhone(configured = false)
        phone.family.load(); phone.family.refresh()
        assertEquals(FamilyState.NotConfigured, phone.family.state.value)
        assertNull(phone.family.sync())
    }

    @Test
    fun `first user creates the family and their data goes to the server`() = runTest {
        val owner = TestPhone()
        owner.accounts.saveAccount(Account("cash", "Наличные", usd))
        owner.family.signIn("owner")
        assertEquals(FamilyState.NoAccess("owner@gmail.com"), owner.family.state.value)

        assertTrue(owner.family.createFamily())
        assertIs<FamilyState.Joined>(owner.family.state.value)
        owner.family.sync()
        assertEquals(listOf("cash"), records.records.map { it.record.id })
    }

    @Test
    fun `stranger without invite cannot create a second family`() = runTest {
        TestPhone().family.run { signIn("owner"); createFamily() }
        val stranger = TestPhone()
        stranger.family.signIn("stranger")
        assertFalse(stranger.family.createFamily())
        assertEquals(FamilyState.NoAccess("stranger@gmail.com"), stranger.family.state.value)
        assertNull(stranger.family.sync())
    }

    @Test
    fun `invited user replaces own data with the family's after a safety copy`() = runTest {
        val owner = TestPhone()
        owner.accounts.saveAccount(Account("family-acc", "Общий", usd))
        owner.family.signIn("owner"); owner.family.createFamily(); owner.family.sync()
        owner.family.invite("Wife@Gmail.com")

        val wife = TestPhone()
        wife.accounts.saveAccount(Account("own-acc", "Свой", usd))
        wife.family.signIn("wife")
        assertIs<FamilyState.Invited>(wife.family.state.value)
        assertTrue(wife.family.hasLocalData().not(), "accounts without operations are not «own data»")

        wife.family.join(keepLocalData = false)
        wife.family.sync()

        assertTrue(wife.backups.single().contains("own-acc"))
        assertNull(wife.accounts.getAccount("own-acc"))
        assertNotNull(wife.accounts.getAccount("family-acc"))
        assertEquals(2, familyServer.family!!.members.size)
    }

    @Test
    fun `invited user may keep own data and send it to the family`() = runTest {
        TestPhone().family.run { signIn("owner"); createFamily(); invite("wife@gmail.com") }
        val wife = TestPhone()
        wife.accounts.saveAccount(Account("own-acc", "Свой", usd))
        wife.family.signIn("wife")
        wife.family.join(keepLocalData = true)
        wife.family.sync()
        assertTrue(records.records.any { it.record.id == "own-acc" })
        assertTrue(wife.backups.isEmpty())
    }

    @Test
    fun `membership survives restart and network errors`() = runTest {
        val owner = TestPhone()
        owner.family.signIn("owner"); owner.family.createFamily()

        val restarted = owner.service()
        restarted.load()
        assertIs<FamilyState.Joined>(restarted.state.value)

        familyServer.offline = true
        restarted.refresh()
        assertIs<FamilyState.Joined>(restarted.state.value, "offline does not throw the user out")
    }

    @Test
    fun `offline check without cached membership says offline`() = runTest {
        val phone = TestPhone("someone")
        familyServer.offline = true
        phone.family.load(); phone.family.refresh()
        assertEquals(FamilyState.Offline("someone@gmail.com"), phone.family.state.value)
    }

    @Test
    fun `sign out forgets membership but keeps local data`() = runTest {
        val owner = TestPhone()
        owner.accounts.saveAccount(Account("cash", "Наличные", usd))
        owner.family.signIn("owner"); owner.family.createFamily()
        owner.family.signOut()
        assertEquals(FamilyState.SignedOut, owner.family.state.value)
        assertNotNull(owner.accounts.getAccount("cash"))
        owner.service().apply { load() }.also { assertEquals(FamilyState.SignedOut, it.state.value) }
    }

    @Test
    fun `lone member re-reads the member list at most once a minute`() = runTest {
        val owner = TestPhone()
        owner.family.signIn("owner"); owner.family.createFamily()
        val wife = TestPhone("wife")
        owner.family.invite("wife@gmail.com")
        wife.family.load(); wife.family.refresh()

        val before = familyServer.familyReads
        repeat(6) { owner.family.sync(); owner.now += 10.seconds } // a minute of 10-second polls
        assertEquals(before + 1, familyServer.familyReads)

        wife.family.join(keepLocalData = true)
        owner.family.sync()
        assertEquals(2, owner.family.joined!!.members.size) // the minute passed: the wife appears
        val reads = familyServer.familyReads
        repeat(3) { owner.family.sync() }
        assertEquals(reads, familyServer.familyReads) // two members: no more member checks
    }
}
