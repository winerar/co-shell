package app.coshell.core.data.family

import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.sync.ChangeBell
import app.coshell.core.data.sync.FakeRemoteStore
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.data.sync.SyncIssue
import app.coshell.core.data.sync.SyncIssueKind
import app.coshell.core.data.remote.ApiException
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.days
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Family
import app.coshell.core.model.Member
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
        var rings = 0
        var bellDown = false
        private val bell = ChangeBell { if (bellDown) error("RTDB down") else rings++ }
        fun service(configured: Boolean = true) = FamilyService(
            configured, auth, FakeFamilyRemote(familyServer, auth), db.syncDao(),
            SyncEngine(db.syncDao(), records, clock), JsonBackupRepository(db, stamper), clock, bell,
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
    fun `stranger without invite has no access but may create their own family`() = runTest {
        TestPhone().family.run { signIn("owner"); createFamily() }
        val stranger = TestPhone()
        stranger.family.signIn("stranger")
        assertEquals(FamilyState.NoAccess("stranger@gmail.com"), stranger.family.state.value)
        assertNull(stranger.family.sync())
        assertTrue(stranger.family.createFamily())
        assertEquals(2, familyServer.families.size)
        val own = (stranger.family.state.value as FamilyState.Joined).family
        assertEquals(listOf("stranger"), own.members.map { it.uid })
        assertTrue(own.id.length >= 20 && own.id != "main", "new families get a long random id")
    }

    @Test
    fun `member of the old main family gets a pointer and stays in it`() = runTest {
        familyServer.family = Family("main", "owner", listOf(Member("owner", "Owner", "owner@gmail.com")))
        val owner = TestPhone()
        owner.family.signIn("owner")
        assertEquals("main", (owner.family.state.value as FamilyState.Joined).family.id)
        assertEquals("main", familyServer.pointers["owner"])
    }

    @Test
    fun `joining uses up the invitation`() = runTest {
        TestPhone().family.run { signIn("owner"); createFamily(); invite("wife@gmail.com") }
        val wife = TestPhone()
        wife.family.signIn("wife"); wife.family.join(keepLocalData = true)
        assertTrue(familyServer.invites.isEmpty())
    }

    private suspend fun ownerWithWife(): Pair<TestPhone, TestPhone> {
        val owner = TestPhone()
        owner.family.signIn("owner"); owner.family.createFamily(); owner.family.invite("wife@gmail.com")
        val wife = TestPhone()
        wife.accounts.saveAccount(Account("own-acc", "Свой", usd))
        wife.family.signIn("wife"); wife.family.join(keepLocalData = true); wife.family.sync()
        return owner to wife
    }

    @Test
    fun `a member leaves and keeps the data, but the owner cannot leave`() = runTest {
        val (owner, wife) = ownerWithWife()
        assertFailsWith<IllegalStateException> { owner.family.leave() }

        wife.family.leave()
        assertEquals(FamilyState.NoAccess("wife@gmail.com"), wife.family.state.value)
        assertNull(wife.family.sync())
        assertNotNull(wife.accounts.getAccount("own-acc"))
        assertNull(familyServer.pointers["wife"])
        assertEquals(listOf("owner"), familyServer.family!!.members.map { it.uid })
        assertEquals("", wife.db.syncDao().state(SyncEngine.CURSOR).orEmpty(), "the pull cursor belongs to the old family")
    }

    @Test
    fun `the owner removes a member, whose phone learns it and may start its own family`() = runTest {
        val (owner, wife) = ownerWithWife()
        assertFailsWith<IllegalStateException> { wife.family.removeMember("owner") }

        owner.family.removeMember("wife")
        assertEquals(listOf("owner"), owner.family.joined!!.members.map { it.uid })

        wife.family.refresh()
        assertEquals(FamilyState.Removed("wife@gmail.com"), wife.family.state.value)
        assertNull(wife.family.sync())
        assertNotNull(wife.accounts.getAccount("own-acc"))
        assertTrue(wife.family.createFamily())
        assertIs<FamilyState.Joined>(wife.family.state.value)
        assertEquals(2, familyServer.families.size)
    }

    @Test
    fun `a removed person who signs out leaves no pointer behind`() = runTest {
        val (owner, wife) = ownerWithWife()
        owner.family.removeMember("wife")
        wife.family.refresh()
        wife.family.signOut()
        assertNull(familyServer.pointers["wife"])
        wife.family.signIn("wife")
        assertEquals(FamilyState.NoAccess("wife@gmail.com"), wife.family.state.value)
    }

    @Test
    fun `the owner deletes the family and the others see they were removed`() = runTest {
        val (owner, wife) = ownerWithWife()
        assertFailsWith<IllegalStateException> { wife.family.deleteFamily() }

        owner.family.deleteFamily()
        assertTrue(familyServer.families.isEmpty())
        assertEquals(FamilyState.NoAccess("owner@gmail.com"), owner.family.state.value)
        wife.family.refresh()
        assertEquals(FamilyState.Removed("wife@gmail.com"), wife.family.state.value)
        assertNotNull(wife.accounts.getAccount("own-acc"))
    }

    @Test
    fun `deleting the account removes the cloud side and keeps the data on the phone`() = runTest {
        val (owner, wife) = ownerWithWife()
        wife.accounts.saveAccount(Account("cash", "Наличные", usd))
        wife.family.deleteAccount()
        assertTrue(wife.auth.accountDeleted)
        assertEquals(FamilyState.SignedOut, wife.family.state.value)
        assertNull(familyServer.pointers["wife"])
        assertEquals(listOf("owner"), familyServer.family!!.members.map { it.uid })
        assertNotNull(wife.accounts.getAccount("cash"))

        owner.family.deleteAccount()
        assertTrue(familyServer.families.isEmpty())
        assertTrue(owner.auth.accountDeleted)
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

    @Test
    fun `rings the bell only after sending edits, and a dead bell does not break sync`() = runTest {
        val owner = TestPhone()
        owner.family.signIn("owner"); owner.family.createFamily()
        owner.family.sync()
        assertEquals(0, owner.rings) // nothing to send yet

        owner.accounts.saveAccount(Account("cash", "Наличные", usd))
        owner.family.sync()
        assertEquals(1, owner.rings)
        owner.family.sync()
        assertEquals(1, owner.rings) // an idle poll does not ring

        owner.bellDown = true
        owner.accounts.saveAccount(Account("card", "Карта", usd))
        assertEquals(1, owner.family.sync()!!.pushed)
    }

    @Test
    fun `quota banner stays while sending is paused and goes once the edits are sent`() = runTest {
        val owner = TestPhone()
        owner.family.signIn("owner"); owner.family.createFamily()
        owner.family.sync()
        suspend fun issue() = SyncIssue.decode(owner.db.syncDao().state(SyncIssue.KEY))

        records.writesLeft = 0
        owner.accounts.saveAccount(Account("cash", "Наличные", usd))
        assertFailsWith<ApiException> { owner.family.sync() }
        assertEquals(SyncIssueKind.QUOTA, issue()?.kind)

        assertTrue(owner.family.sync()!!.pushPaused) // a poll during the pause does not hide the banner
        assertEquals(SyncIssueKind.QUOTA, issue()?.kind)

        records.writesLeft = 20_000
        owner.now += 1.days
        assertEquals(1, owner.family.sync()!!.pushed)
        assertNull(issue())
    }
}
