package app.coshell.core.data.family

import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.remote.ApiException
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.sync.FakeRemoteStore
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.data.sync.SyncIssue
import app.coshell.core.data.sync.SyncIssueKind
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class SyncIssueTest {
    private val records = FakeRemoteStore()
    private val auth = FakeAuth()
    private val db = createInMemoryCoShellDatabase()
    private var now = Instant.fromEpochMilliseconds(1_700_000_000_000)
    private val clock = object : Clock { override fun now() = this@SyncIssueTest.now }
    private val stamper = WriteStamper { auth.uid }
    private val family = FamilyService(
        true, auth, FakeFamilyRemote(FakeFamilyServer(), auth), db.syncDao(),
        SyncEngine(db.syncDao(), records), JsonBackupRepository(db, stamper), clock,
    ) {}

    @AfterTest
    fun tearDown() = db.close()

    private suspend fun issue() = SyncIssue.decode(db.syncDao().state(SyncIssue.KEY))

    @Test
    fun `server answers map to understandable reasons`() {
        assertEquals(SyncIssueKind.OFFLINE to null, SyncIssue.classify(IllegalStateException("timeout")))
        assertEquals(SyncIssueKind.SIGNED_OUT, SyncIssue.classify(ApiException(401, "UNAUTHENTICATED", "")).first)
        assertEquals(SyncIssueKind.NO_ACCESS to "PERMISSION_DENIED", SyncIssue.classify(ApiException(403, "PERMISSION_DENIED", "")))
        assertEquals(SyncIssueKind.QUOTA, SyncIssue.classify(ApiException(429, "RESOURCE_EXHAUSTED", "")).first)
        assertEquals(SyncIssueKind.SERVER to "HTTP 503", SyncIssue.classify(ApiException(503, null, "")))
    }

    @Test
    fun `encoded issue survives a round trip and junk reads as none`() {
        val issue = SyncIssue(SyncIssueKind.QUOTA, now, "RESOURCE_EXHAUSTED")
        assertEquals(issue, SyncIssue.decode(issue.encode()))
        assertEquals(SyncIssue(SyncIssueKind.OFFLINE, now), SyncIssue.decode(SyncIssue(SyncIssueKind.OFFLINE, now).encode()))
        assertNull(SyncIssue.decode(""))
        assertNull(SyncIssue.decode("NOPE|1|"))
    }

    @Test
    fun `failed syncs keep the first failure time and a good sync clears the issue`() = runTest {
        family.signIn("owner"); family.createFamily()
        records.failNextPull = true
        assertFailsWith<IllegalStateException> { family.sync() }
        val first = now
        now += 3.minutes
        records.failNextPull = true
        assertFailsWith<IllegalStateException> { family.sync() }
        assertEquals(SyncIssue(SyncIssueKind.OFFLINE, first), issue())

        now += 1.minutes
        records.failNextPullWith = ApiException(429, "RESOURCE_EXHAUSTED", "quota")
        assertFailsWith<ApiException> { family.sync() }
        assertEquals(SyncIssue(SyncIssueKind.QUOTA, now, "RESOURCE_EXHAUSTED"), issue())

        family.sync()
        assertNull(issue())
    }

    @Test
    fun `expired sign-in leaves a reason for the banner until the next sign-in`() = runTest {
        family.signIn("owner"); family.createFamily()
        records.failNextPullWith = ApiException(401, "UNAUTHENTICATED", "expired")
        assertFailsWith<ApiException> { family.sync() }
        assertEquals(SyncIssueKind.SIGNED_OUT, issue()?.kind)

        family.signIn("owner")
        assertNull(issue())
    }
}
